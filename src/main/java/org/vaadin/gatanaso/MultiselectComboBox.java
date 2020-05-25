package org.vaadin.gatanaso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasFilterableDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * A multiselection component where items are displayed in a drop-down list.
 *
 * <p>
 * This is the server-side component for the `multiselect-combo-box`
 * web component. It contains the same features as the web component, such as
 * displaying, selection and filtering of multiple items from a drop-down list.
 * </p>
 *
 * <p>
 * MultiselectComboBox supports lazy loading. This means that when using large
 * data sets, items are requested from the server one "page" at a time when the
 * user scrolls down the overlay. The number of items in one page is by default
 * 50, and can be changed with {@link #setPageSize(int)}.
 * <p>
 * MultiselectComboBox can do filtering either in the browser or in the server.
 * When MultiselectComboBox has only a relatively small set of items, the
 * filtering will happen in the browser, allowing smooth user-experience. When
 * the size of the data set is larger than the {@code pageSize}, the
 * web component doesn't necessarily have all the data available and it will make
 * requests to the server to handle the filtering. Also, if you have defined
 * custom filtering logic, with eg. {@link #setItems(ItemFilter, Collection)},
 * filtering will happen in the server. To enable client-side filtering with
 * larger data sets, you can override the {@code pageSize} to be bigger than the
 * size of your data set. However, then the full data set will be sent to the
 * client immediately and you will lose the benefits of lazy loading.
 *
 * @param <T>
 *            the type of the items to be inserted in the multiselect combo box
 *
 * @author gatanaso
 */
@Tag("multiselect-combo-box")
@NpmPackage(value = "multiselect-combo-box", version = "2.4.2")
@JsModule("multiselect-combo-box/src/multiselect-combo-box.js")
@JavaScript("frontend://multiselectComboBoxConnector.js")
@JsModule("./multiselectComboBoxConnector-es6.js")
public class MultiselectComboBox<T>
        extends AbstractSinglePropertyField<MultiselectComboBox<T>, Set<T>>
        implements HasStyle, HasSize, HasValidation, HasEnabled,
        MultiSelect<MultiselectComboBox<T>, T>,
        HasFilterableDataProvider<T, String> {

    protected static final String ITEM_VALUE_PATH = "key";
    protected static final String ITEM_LABEL_PATH = "label";

    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();

    /**
     * Lazy loading updater, used when calling setDataProvider()
     */
    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new UpdateQueue(sizeChange);
        }

        @Override
        public void initialize() {
            // NO-OP
        }
    };

    private MultiselectComboBoxDataCommunicator<T> dataCommunicator;
    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;
    private Registration dataGeneratorRegistration;

    private Renderer<T> renderer;
    private boolean renderScheduled;
    private Element template;

    private int customValuesListenersCount;

    private SerializableConsumer<String> filterSlot = filter -> {
        // Just ignore when setDataProvider has not been called
    };

    // Filter set by the client when requesting data. It's sent back to client
    // together with the response so client may know for what filter data is
    // provided.
    private String lastFilter;

    private UserProvidedFilter userProvidedFilter = UserProvidedFilter.UNDECIDED;

    /**
     * Default constructor. Creates an empty multiselect combo box.
     */
    public MultiselectComboBox() {
        this(50);
    }

    /**
     * Creates an empty multiselect combo box with the defined page size for
     * lazy loading.
     * <p>
     * The default page size is 50.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     *
     * @param pageSize
     *            the amount of items to request at a time for lazy loading
     * @see #setPageSize
     */
    public MultiselectComboBox(int pageSize) {
        super("selectedItems", Collections.emptySet(), JsonArray.class,
                MultiselectComboBox::presentationToModel,
                MultiselectComboBox::modelToPresentation);

        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject
                .put(ITEM_LABEL_PATH, generateLabel(item)));

        setItemIdPath(ITEM_VALUE_PATH);
        setItemValuePath(ITEM_VALUE_PATH);
        setItemLabelPath(ITEM_LABEL_PATH);
        setPageSize(pageSize);

        addAttachListener(e -> initConnector());

        runBeforeClientResponse(ui -> {
            // If user didn't provide any data, initialize with empty data set.
            if (dataCommunicator == null) {
                setItems();
            }
        });
    }

    /**
     * Creates an empty multiselect combo box with the defined label.
     *
     * @param label
     *            the label describing the combo box
     */
    public MultiselectComboBox(String label) {
        this();
        setLabel(label);
    }

    /**
     * Creates a multiselect combo box with the defined label and populated with
     * the items in the collection.
     *
     * @param label
     *            the label describing the combo box
     * @param items
     *            the items to be shown in the list of the combo box
     * @see #setItems(Collection)
     */
    public MultiselectComboBox(String label, Collection<T> items) {
        this();
        setLabel(label);
        setItems(items);
    }

    /**
     * Creates a multiselect combo box with the defined label and populated with
     * the items in the array.
     *
     * @param label
     *            the label describing the combo box
     * @param items
     *            the items to be shown in the list of the combo box
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public MultiselectComboBox(String label, T... items) {
        this();
        setLabel(label);
        setItems(items);
    }

    private static <T> Set<T> presentationToModel(
            MultiselectComboBox<T> multiselectComboBox,
            JsonArray presentation) {

        if (presentation == null || multiselectComboBox.dataCommunicator == null) {
            return multiselectComboBox.getEmptyValue();
        }

        if (multiselectComboBox.getValue() != null) {
            // keep existing value items in keyMapper
            multiselectComboBox.getValue().forEach(item -> multiselectComboBox.getKeyMapper().key(item));
        }

        Set<T> set = new HashSet<>();
        for (int i = 0; i < presentation.length(); i++) {
            String key = presentation.getObject(i).getString(ITEM_VALUE_PATH);
            set.add(multiselectComboBox.getKeyMapper().get(key));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(
            MultiselectComboBox<T> multiselectComboBox, Set<T> model) {
        JsonArray array = Json.createArray();
        if (model == null || model.isEmpty()) {
            return array;
        }

        model.stream().map(multiselectComboBox::generateJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
    }

    @Override
    public void setValue(Set<T> value) {
        if (dataCommunicator == null) {
            if (value == null) {
                return;
            } else {
                throw new IllegalStateException(
                        "Cannot set a value for a MultiselectComboBox without items. "
                                + "Use setItems or setDataProvider to populate "
                                + "items into the MultiselectComboBox before setting a value.");
            }
        }
        super.setValue(value);
        refreshValue();
    }

    private void refreshValue() {
        Set<T> value = getValue();
        if (value == null || value.isEmpty()) {
            return;
        }
        JsonArray selectedItems = modelToPresentation(this, value);
        getElement().setPropertyJson("selectedItems", selectedItems);
    }

    /**
     * Sets the TemplateRenderer responsible to render the individual items in
     * the list of possible choices of the MultiselectComboBox. It doesn't affect how the
     * selected item is rendered - that can be configured by using
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     *
     * @param renderer
     *            a renderer for the items in the selection list of the
     *            MultiselectComboBox, not <code>null</code>
     *
     * Note that filtering of the MultiselectComboBox is not affected by the renderer that
     * is set here. Filtering is done on the original values and can be affected
     * by {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     */
    public void setRenderer(Renderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");
        this.renderer = renderer;

        if (template == null) {
            template = new Element("template");
            getElement().appendChild(template);
        }
        scheduleRender();
    }

    /**
     * Gets the label of the multiselect-combo-box.
     *
     * @return the label property of the multiselect-combo-box.
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Sets the label of the multiselect-combo-box.
     *
     * @param label
     *            the String value to set.
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the title of the multiselect-combo-box.
     *
     * @return the title property of the multiselect-combo-box.
     */
    public String getTitle() {
        return getElement().getProperty("title");
    }

    /**
     * Gets the placeholder of the {@code multiselect-combo-box}.
     *
     * @return the placeholder property of the multiselect-combo-box.
     */
    public String getPlaceholder() {
        return getElement().getProperty("placeholder");
    }

    /**
     * Sets the placeholder of the multiselect-combo-box.
     *
     * @param placeholder
     *            the String value to set.
     */
    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder",
                placeholder == null ? "" : placeholder);
    }

    /**
     * Gets the required value of the multiselect-combo-box.
     *
     * @return true if the component is required, false otherwise.
     */
    public boolean isRequired() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Sets the required value of the multiselect-combo-box.
     *
     * @param required
     *            the boolean value to set
     */
    public void setRequired(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets the 'compact-mode' property value of the multiselect-combo-box.
     *
     * @return true if the component is in 'compact-mode', false otherwise.
     */
    public boolean isCompactMode() {
        return getElement().getProperty("compactMode", false);
    }

    /**
     * Sets the 'compact-mode' property value of the multiselect-combo-box.
     *
     * @param compactMode
     *            the boolean value to set
     */
    public void setCompactMode(boolean compactMode) {
        getElement().setProperty("compactMode", compactMode);
    }

    /**
     * Gets the 'ordered' property value of the multiselect-combo-box.
     *
     * @return true if the component is ordered, false otherwise.
     */
    public boolean isOrdered() {
        return getElement().getProperty("ordered", false);
    }

    /**
     * Sets the 'ordered' property value of the multiselect-combo-box.
     *
     * This attribute specifies if the list of selected items should be kept
     * ordered in ascending lexical order.
     *
     * @param ordered
     *            the boolean value to set
     */
    public void setOrdered(boolean ordered) {
        getElement().setProperty("ordered", ordered);
    }

    /**
     * Gets the validity of the multiselect-combo-box.
     *
     * @return true if the component is invalid, false otherwise.
     */
    @Override
    @Synchronize(property = "invalid", value = "invalid-changed")
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * Sets the invalid value of the multiselect-combo-box.
     *
     * @param invalid
     *            the boolean value to set.
     */
    @Override
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * Gets the current error message of the multiselect-combo-box.
     *
     * @return the current error message
     */
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Sets the error message of the multiselect-combo-box.
     * <p>
     * The error message is displayed when the component is invalid.
     *
     * @param errorMessage
     *            the String value to set
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * <p>
     * Set to true to display the clear icon which clears the input.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code clearButtonVisible} property from the web component
     */
    public boolean isClearButtonVisible() {
        return getElement().getProperty("clearButtonVisible", false);
    }

    /**
     * <p>
     * Set to true to display the clear icon which clears the input.
     * </p>
     *
     * @param clearButtonVisible
     *            the boolean value to set
     */
    public void setClearButtonVisible(boolean clearButtonVisible) {
        getElement().setProperty("clearButtonVisible", clearButtonVisible);
    }

    /**
     * Gets the value of the configured value separator when in read only mode.
     *
     * @return the read only value separator.
     */
    public String getReadOnlyValueSeparator() {
        return getElement().getProperty("readonlyValueSeparator");
    }

    /**
     * Sets the value separator when in read only mode.
     *
     * @param readonlyValueSeparator
     *            the separator value to set
     */
    public void setReadOnlyValueSeparator(String readonlyValueSeparator) {
        getElement().setProperty("readonlyValueSeparator", readonlyValueSeparator == null ? "" : readonlyValueSeparator);
    }

    private void setItemValuePath(String itemValuePath) {
        getElement().setProperty("itemValuePath",
                itemValuePath == null ? "" : itemValuePath);
    }

    private void setItemLabelPath(String itemLabelPath) {
        getElement().setProperty("itemLabelPath",
                itemLabelPath == null ? "" : itemLabelPath);
    }

    private void setItemIdPath(String itemIdPath) {
        getElement().setProperty("itemIdPath",
                itemIdPath == null ? "" : itemIdPath);
    }

    /**
     * Gets the item label generator.
     *
     * By default, {@link String#valueOf(Object)} is used.
     *
     * @return the item label generator.
     */
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the multiselect-combo-box for each item. By default,
     * {@link String#valueOf(Object)} is used.
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        reset();
        if (getValue() != null) {
            refreshValue();
        }
    }

    /**
     * Sets the page size, which is the number of items requested at a time from
     * the data provider. This does not guarantee a maximum query size to the
     * backend; when the overlay has room to render more new items than the page
     * size, multiple "pages" will be requested at once.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     * <p>
     * Setting the page size after the MultiselectComboBox has been rendered
     * effectively resets the component, and the current page(s) and sent over
     * again.
     * <p>
     * The default page size is 50.
     *
     * @param pageSize
     *            the maximum number of items sent per request, should be
     *            greater than zero
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException(
                    "Page size should be greater than zero.");
        }
        getElement().setProperty("pageSize", pageSize);
        reset();
    }

    /**
     * Gets the page size, which is the number of items fetched at a time from
     * the data provider.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     * <p>
     * The default page size is 50.
     *
     * @return the maximum number of items sent per request
     * @see #setPageSize
     */
    public int getPageSize() {
        return getElement().getProperty("pageSize", 50);
    }

    /**
     * Enables or disables the component firing events for custom string input.
     * <p>
     * When enabled, a {@link CustomValuesSetEvent} will be fired when the user
     * inputs a string value that does not match any existing items and commits
     * it eg. by blurring or pressing the enter-key.
     * <p>
     * Note that MultiselectComboBox doesn't do anything with the custom value string
     * automatically. Use the
     * {@link #addCustomValuesSetListener(ComponentEventListener)} method to
     * determine how the custom value should be handled. For example, when the
     * MultiselectComboBox has {@code String} as the value type, you can add a listener
     * which sets the custom string as on of the values of the MultiselectComboBox.
     *
     * @param allowCustomValues
     *            {@code true} to enable custom values set events, {@code false}
     *            to disable them
     *
     * @see #addCustomValuesSetListener(ComponentEventListener)
     */
    public void setAllowCustomValues(boolean allowCustomValues) {
        getElement().setProperty("allowCustomValues", allowCustomValues);
    }

    /**
     * If {@code true}, the user can input string values that do not match to
     * any existing item labels, which will fire a {@link CustomValuesSetEvent}.
     *
     * @return {@code true} if the component fires custom value set events,
     *         {@code false} otherwise
     *
     * @see #setAllowCustomValues(boolean)
     * @see #addCustomValuesSetListener(ComponentEventListener)
     */
    public boolean isAllowCustomValues() {
        return getElement().getProperty("allowCustomValues", false);
    }

    /**
     * Gets the data provider used by this {@link MultiselectComboBox}.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataCommunicator.getDataProvider();
    }

    private void reset() {
        lastFilter = null;
        if (dataCommunicator != null) {
            dataCommunicator.setRequestedRange(0, 0);
            dataCommunicator.reset();
        }
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
                // If-statement is needed because on the first attach this
                // JavaScript is called before initializing the connector.
                "if($0.$connector) $0.$connector.reset();", getElement()));
    }

    private String generateLabel(T item) {
        if (item == null) {
            return "";
        }
        String label = getItemLabelGenerator().apply(item);
        if (label == null) {
            throw new IllegalStateException(String.format(
                    "Got 'null' as a label value for the item '%s'. "
                            + "'%s' instance may not return 'null' values",
                    item, ItemLabelGenerator.class.getSimpleName()));
        }
        return label;
    }

    private JsonObject generateJson(T item) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put(ITEM_VALUE_PATH, getKeyMapper().key(item));
        dataGenerator.generateData(item, jsonObject);
        return jsonObject;
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.multiselectComboBoxConnector.initLazy(this)");
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Set<T> value = new HashSet<>(getValue());
        value.addAll(addedItems);
        value.removeAll(removedItems);
        setValue(value);
    }

    @Override
    public Set<T> getSelectedItems() {
        return getValue();
    }

    @Override
    public Registration addSelectionListener(
            MultiSelectionListener<MultiselectComboBox<T>, T> listener) {
        return addValueChangeListener(event -> listener
                .selectionChange(new MultiSelectionEvent<>(this, this,
                        event.getOldValue(), event.isFromClient())));
    }

    /**
     * Adds a listener for the event which is fired when user inputs a string
     * value that does not match any existing items and commits it eg. by
     * blurring or pressing the enter-key.
     * <p>
     * Note that MultiselectComboBox doesn't do anything with the custom value string
     * automatically. Use this method to determine how the custom value should
     * be handled. For example, when the MultiselectComboBox has {@code String} as the
     * value type, you can add a listener which sets the custom string as on of the
     * values of the MultiselectComboBox.
     * <p>
     * As a side effect, this makes the MultiselectComboBox allow custom values. If you
     * want to disable the firing of custom value set events once the listener
     * is added, please disable it explicitly via the
     * {@link #setAllowCustomValues(boolean)} method.
     * <p>
     * The custom value becomes disallowed automatically once the last custom
     * value set listener is removed.
     *
     * @param listener
     *            the listener to be notified when a new value is filled
     * @return a {@link Registration} for removing the event listener
     *
     * @see #setAllowCustomValues(boolean)
     */
    public Registration addCustomValuesSetListener(ComponentEventListener<CustomValuesSetEvent<MultiselectComboBox<T>>> listener) {
        setAllowCustomValues(true);
        customValuesListenersCount++;
        Registration registration = addListener(CustomValuesSetEvent.class, (ComponentEventListener) listener);
        return new CustomValuesRegistration(registration);
    }

    private DataKeyMapper<T> getKeyMapper() {
        return dataCommunicator.getKeyMapper();
    }

    private void scheduleRender() {
        if (renderScheduled || dataCommunicator == null || renderer == null) {
            return;
        }
        renderScheduled = true;

        runBeforeClientResponse(ui -> {
            if (dataGeneratorRegistration != null) {
                dataGeneratorRegistration.remove();
                dataGeneratorRegistration = null;
            }

            Rendering<T> rendering = renderer.render(getElement(), dataCommunicator.getKeyMapper(), template);

            if (rendering.getDataGenerator().isPresent()) {
                dataGeneratorRegistration = dataGenerator
                    .addDataGenerator(rendering.getDataGenerator().get());
            }

            reset();
        });
    }

    @ClientCallable
    private void confirmUpdate(int id) {
        dataCommunicator.confirmUpdate(id);
    }

    @ClientCallable
    private void setRequestedRange(int start, int length, String filter) {
        dataCommunicator.setRequestedRange(start, length);
        filterSlot.accept(filter);
    }

    @ClientCallable
    private void resetDataCommunicator() {
        dataCommunicator.reset();
    }

    @ClientCallable
    private void initDataConnector() {
        // init data connector when shadow-dom is ready
        getElement().executeJs("$0.$connector.initDataConnector()");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Filtering will use a case insensitive match to show all items where the
     * filter text is a substring of the label displayed for that item, which
     * you can configure with
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     * <p>
     * Filtering will be handled in the client-side if the size of the data set
     * is less than the page size. To force client-side filtering with a larger
     * data set (at the cost of increased network traffic), you can increase the
     * page size with {@link #setPageSize(int)}.
     * <p>
     * Setting the items creates a new DataProvider, which in turn resets the
     * multiselect combo box's value to {@code null}. If you want to add and
     * remove items to the current item set without resetting the value, you
     * should update the previously set item collection and call
     * {@code getDataProvider().refreshAll()}.
     */
    @Override
    public void setItems(Collection<T> items) {
        setDataProvider(DataProvider.ofCollection(items));
    }

    /**
     * Sets the data items of this multiselect combo box and a filtering
     * function for defining which items are displayed when user types into the
     * combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server round trips to handle the filtering. Otherwise, it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Setting the items creates a new DataProvider, which in turn resets the
     * combo box's value to {@code null}. If you want to add and remove items to
     * the current item set without resetting the value, you should update the
     * previously set item collection and call
     * {@code getDataProvider().refreshAll()}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the MultiselectComboBox
     * @param items
     *            the data items to display
     */
    public void setItems(ItemFilter<T> itemFilter, Collection<T> items) {
        ListDataProvider<T> listDataProvider = DataProvider.ofCollection(items);

        setDataProvider(itemFilter, listDataProvider);
    }

    /**
     * Sets the data items of this multiselect combo box and a filtering
     * function for defining which items are displayed when user types into the
     * combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server round trips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Setting the items creates a new DataProvider, which in turn resets the
     * combo box's value to {@code null}. If you want to add and remove items to
     * the current item set without resetting the value, you should update the
     * previously set item collection and call
     * {@code getDataProvider().refreshAll()}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the MultiselectComboBox
     * @param items
     *            the data items to display
     */
    public void setItems(ItemFilter<T> itemFilter, T... items) {
        setItems(itemFilter, Arrays.asList(items));
    }

    /**
     * {@inheritDoc}
     * <p>
     * The filter-type of the given data provider must be String so that it can
     * handle the filters typed into the MultiselectComboBox by users. If your
     * data provider uses some other type of filter, you can provide a function
     * which converts the MultiselectComboBox's filter-string into that type via
     * {@link #setDataProvider(DataProvider, SerializableFunction)}. Another way
     * to do the same thing is to use this method with your data provider
     * converted with
     * {@link DataProvider#withConvertedFilter(SerializableFunction)}.
     * <p>
     * Changing the multiselect combo box's data provider resets its current
     * value to {@code null}.
     */
    @Override
    public void setDataProvider(DataProvider<T, String> dataProvider) {
        setDataProvider(dataProvider, SerializableFunction.identity());
    }

    /**
     * {@inheritDoc}
     * <p>
     * MultiselectComboBox triggers filtering queries based on the strings users
     * type into the field. For this reason you need to provide the second
     * parameter, a function which converts the filter-string typed by the user
     * into filter-type used by your data provider. If your data provider
     * already supports String as the filter-type, it can be used without a
     * converter function via {@link #setDataProvider(DataProvider)}.
     * <p>
     * Using this method provides the same result as using a data provider
     * wrapped with
     * {@link DataProvider#withConvertedFilter(SerializableFunction)}.
     * <p>
     * Changing the multiselect combo box's data provider resets its current
     * value to {@code null}.
     */
    @Override
    public <C> void setDataProvider(DataProvider<T, C> dataProvider,
            SerializableFunction<String, C> filterConverter) {

        Objects.requireNonNull(dataProvider,
                "The data provider can not be null");
        Objects.requireNonNull(filterConverter,
                "filterConverter cannot be null");

        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.YES;
        }

        if (dataCommunicator == null) {
            dataCommunicator = new MultiselectComboBoxDataCommunicator<>(dataGenerator,
                    arrayUpdater, data -> getElement()
                            .callJsFunction("$connector.updateData", data),
                    getElement().getNode());
        }

        scheduleRender();
        setValue(null);

        SerializableFunction<String, C> convertOrNull = filterText -> {
            if (filterText == null) {
                return null;
            }

            return filterConverter.apply(filterText);
        };

        SerializableConsumer<C> providerFilterSlot = dataCommunicator
                .setDataProvider(dataProvider, convertOrNull.apply(null));

        filterSlot = filter -> {
            if (!Objects.equals(filter, lastFilter)) {
                providerFilterSlot.accept(convertOrNull.apply(filter));
                lastFilter = filter;
            }
        };

        boolean shouldForceServerSideFiltering = userProvidedFilter == UserProvidedFilter.YES;

        dataProvider.addDataProviderListener(e -> {
            if (e instanceof DataChangeEvent.DataRefreshEvent) {
                dataCommunicator.refresh(
                        ((DataChangeEvent.DataRefreshEvent<T>) e).getItem());
            } else {
                refreshAllData(shouldForceServerSideFiltering);
            }
        });
        refreshAllData(shouldForceServerSideFiltering);

        userProvidedFilter = UserProvidedFilter.UNDECIDED;
    }

    /**
     * Sets a list data provider as the data provider of this multiselect combo
     * box.
     * <p>
     * Filtering will use a case insensitive match to show all items where the
     * filter text is a substring of the label displayed for that item, which
     * you can configure with
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     * <p>
     * Filtering will be handled in the client-side if the size of the data set
     * is less than the page size. To force client-side filtering with a larger
     * data set (at the cost of increased network traffic), you can increase the
     * page size with {@link #setPageSize(int)}.
     * <p>
     * Changing the multiselect combo box's data provider resets its current
     * value to {@code null}.
     *
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     */
    public void setDataProvider(ListDataProvider<T> listDataProvider) {
        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.NO;
        }

        // Cannot use the case insensitive contains shorthand from
        // ListDataProvider since it wouldn't react to locale changes
        ItemFilter<T> defaultItemFilter = (item,
                filterText) -> generateLabel(item).toLowerCase(getLocale())
                        .contains(filterText.toLowerCase(getLocale()));

        setDataProvider(defaultItemFilter, listDataProvider);
    }

    /**
     * Sets a CallbackDataProvider using the given fetch items callback and a
     * size callback.
     * <p>
     * This method is a shorthand for making a {@link CallbackDataProvider} that
     * handles a partial Query object.
     * <p>
     * Changing the multiselect combo box's data provider resets its current
     * value to {@code null}.
     *
     * @param fetchItems
     *            a callback for fetching items
     * @param sizeCallback
     *            a callback for getting the count of items
     *
     * @see CallbackDataProvider
     * @see #setDataProvider(DataProvider)
     */
    public void setDataProvider(FetchItemsCallback<T> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        userProvidedFilter = UserProvidedFilter.YES;
        setDataProvider(new CallbackDataProvider<>(
                q -> fetchItems.fetchItems(q.getFilter().orElse(""),
                        q.getOffset(), q.getLimit()),
                q -> sizeCallback.apply(q.getFilter().orElse(""))));
    }

    /**
     * Sets a list data provider with an item filter as the data provider of
     * this multiselect combo box. The item filter is used to compare each item
     * to the filter text entered by the user.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server round trips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Changing the multiselect combo box's data provider resets its current
     * value to {@code null}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the MultiselectComboBox
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     */
    public void setDataProvider(ItemFilter<T> itemFilter,
            ListDataProvider<T> listDataProvider) {

        Objects.requireNonNull(listDataProvider,
                "List data provider cannot be null");
        setDataProvider(listDataProvider,
                filterText -> item -> itemFilter.test(item, filterText));
    }

    private void refreshAllData(boolean forceServerSideFiltering) {
        setClientSideFilter(!forceServerSideFiltering && getDataProvider()
                .size(new Query<>()) <= getPageSizeDouble());

        reset();
    }

    private double getPageSizeDouble() {
        return getElement().getProperty("pageSize", 0.0);
    }

    private void setClientSideFilter(boolean clientSideFilter) {
        getElement().setProperty("_clientSideFilter", clientSideFilter);
    }

    private enum UserProvidedFilter {
        UNDECIDED, YES, NO
    }

    /**
     * Predicate to check {@link MultiselectComboBox} items against user typed
     * strings.
     */
    @FunctionalInterface
    public interface ItemFilter<T> extends SerializableBiPredicate<T, String> {
        @Override
        public boolean test(T item, String filterText);
    }

    /**
     * A callback method for fetching items. The callback is provided with a
     * non-null string filter, offset index and limit.
     *
     * @param <T>
     *            item (bean) type in MultiselectComboBox
     */
    @FunctionalInterface
    public interface FetchItemsCallback<T> extends Serializable {
        /**
         * Returns a stream of items that match the given filter, limiting the
         * results with given offset and limit.
         *
         * @param filter
         *            a non-null filter string
         * @param offset
         *            the first index to fetch
         * @param limit
         *            the fetched item count
         * @return stream of items
         */
        public Stream<T> fetchItems(String filter, int offset, int limit);
    }

    @DomEvent("custom-values-set")
    public static class CustomValuesSetEvent<T> extends ComponentEvent<MultiselectComboBox<T>> {
        private final String detail;

        public CustomValuesSetEvent(MultiselectComboBox<T> source, boolean fromClient,
            @EventData("event.detail") String detail) {

            super(source, fromClient);
            this.detail = detail;
        }

        public String getDetail() {
            return detail;
        }
    }

    private class CustomValuesRegistration implements Registration {
        private Registration delegate;

        private CustomValuesRegistration(Registration delegate) {
            this.delegate = delegate;
        }

        @Override
        public void remove() {
            if (delegate != null) {
                delegate.remove();
                customValuesListenersCount--;
                if (customValuesListenersCount == 0) {
                    setAllowCustomValues(false);
                }
                delegate = null;
            }
        }
    }

    private final class UpdateQueue implements ArrayUpdater.Update {
        private transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()),
                    MultiselectComboBox.this.lastFilter);
        }

        @Override
        public void clear(int start, int length) {
            // NO-OP
        }

        @Override
        public void commit(int updateId) {
            enqueue("$connector.confirm", updateId,
                    MultiselectComboBox.this.lastFilter);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callJsFunction(name, arguments));
        }
    }
}
