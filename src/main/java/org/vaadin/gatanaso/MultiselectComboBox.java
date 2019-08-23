package org.vaadin.gatanaso;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * A multiselection component where items are displayed in a drop-down list.
 *
 * <p>
 * This is the server-side component for the `multiselect-combo-box`
 * webcomponent. It contains the same features as the webcomponent, such as
 * displaying, selection and filtering of multiple items from a drop-down list.
 * </p>
 *
 * @author gatanaso
 */
@Tag("multiselect-combo-box")
@NpmPackage(value = "multiselect-combo-box", version = "2.0.2")
@JsModule("multiselect-combo-box/src/multiselect-combo-box.js")
public class MultiselectComboBox<T>
        extends AbstractSinglePropertyField<MultiselectComboBox<T>, Set<T>>
        implements HasStyle, HasSize, HasValidation, HasEnabled,
        MultiSelect<MultiselectComboBox<T>, T>, HasDataProvider<T> {

    protected static final String ITEM_VALUE_PATH = "key";
    protected static final String ITEM_LABEL_PATH = "label";
    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;
    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();
    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);
    private Registration dataProviderListenerRegistration;

    /**
     * Default constructor.
     * <p>
     * Creates an empty multiselect combo box.
     * </p>
     */
    public MultiselectComboBox() {
        super("selectedItems", Collections.emptySet(), JsonArray.class,
                MultiselectComboBox::presentationToModel,
                MultiselectComboBox::modelToPresentation);

        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject
                .put(ITEM_LABEL_PATH, generateLabel(item)));

        setItemIdPath(ITEM_VALUE_PATH);
        setItemValuePath(ITEM_VALUE_PATH);
        setItemLabelPath(ITEM_LABEL_PATH);
    }

    private static <T> Set<T> presentationToModel(
            MultiselectComboBox<T> multiselectComboBox,
            JsonArray presentation) {
        JsonArray array = presentation;
        Set<T> set = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            String key = array.getObject(i).getString(ITEM_VALUE_PATH);
            set.add(multiselectComboBox.keyMapper.get(key));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(
            MultiselectComboBox<T> multiselectComboBox, Set<T> model) {
        JsonArray array = Json.createArray();
        if (model.isEmpty()) {
            return array;
        }

        model.stream().map(multiselectComboBox::generateJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
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

    protected void setItems(JsonArray items) {
        getElement().setPropertyJson("items", items);
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
    }

    /**
     * Gets the data provider used by this {@link MultiselectComboBox}.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider,
                "The data provider can not be null");
        this.dataProvider = dataProvider;
        reset();
        updateDataProviderListenerRegistration();
    }

    private void reset() {
        keyMapper.removeAll();
        clear();
        refreshItems();
    }

    private void refreshItems() {
        Set<T> data = dataProvider.fetch(new Query<>())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        setItems(modelToPresentation(this, data));
    }

    private void updateDataProviderListenerRegistration() {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(e -> refreshItems());
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
        jsonObject.put(ITEM_VALUE_PATH, keyMapper.key(item));
        dataGenerator.generateData(item, jsonObject);
        return jsonObject;
    }

    private Object getItemId(T item) {
        if (getDataProvider() == null) {
            return item;
        }
        return getDataProvider().getId(item);
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
}
