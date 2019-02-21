package org.vaadin.gatanaso;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Tag("multiselect-combo-box")
@HtmlImport("bower_components/multiselect-combo-box/multiselect-combo-box.html")
public class MultiselectComboBox<T>
        extends AbstractSinglePropertyField<MultiselectComboBox<T>, Set<T>>
        implements HasStyle, HasSize, HasValidation,
        HasDataProvider<T> {

    public static final String KEY_ATTRIBUTE = "key";
    public static final String LABEL_ATTRIBUTE = "label";

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();

    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);

    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private Registration dataProviderListenerRegistration;

    public MultiselectComboBox() {
        super("selectedItems",
            Collections.emptySet(),
            JsonArray.class,
            MultiselectComboBox::presentationToModel,
            MultiselectComboBox::modelToPresentation);

        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject.put(LABEL_ATTRIBUTE, generateLabel(item)));

        setItemIdPath(KEY_ATTRIBUTE);
        setItemValuePath(KEY_ATTRIBUTE);
        setItemLabelPath(LABEL_ATTRIBUTE);
    }

    /**
     * Gets the label of the multiselect-combo-box.
     *
     * @return the label property of the multiselect-combo-box.
     */
    public String getLabel() {
        return getElement().getProperty(LABEL_ATTRIBUTE);
    }

    /**
     * Sets the label of the multiselect-combo-box.
     *
     * @param label
     *            the String value to set.
     */
    public void setLabel(String label) {
        getElement().setProperty(LABEL_ATTRIBUTE, label == null ? "" : label);
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
        getElement().setProperty("placeholder", placeholder == null ? "" : placeholder);
    }

    /**
     * Gets the required value of the multiselect-combo-box.
     *
     * @return true if the component is required, false otherwise.
     */
    public boolean isRequired() {
        return getElement().getProperty("required", false);
    }

    /**
     * Sets the required value of the multiselect-combo-box.
     *
     * @param required
     *            the boolean value to set
     */
    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
    }

    /**
     * Gets the readonly value of the multiselect-combo-box.
     *
     * @return true if the component is readonly, false otherwise.
     */
    public boolean isReadonly() {
        return getElement().getProperty("readonly", false);
    }

    /**
     * Sets the readonly value of the {@code multiselect-combo-box}.
     *
     * @param readonly
     *            the boolean value to set
     */
    public void setReadonly(boolean readonly) {
        getElement().setProperty("readonly", readonly);
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
        getElement().setProperty("errorMessage", errorMessage == null ? "" : errorMessage);
    }

    private void setItemValuePath(String itemValuePath) {
        getElement().setProperty("itemValuePath", itemValuePath == null ? "" : itemValuePath);
    }

    private void setItemLabelPath(String itemLabelPath) {
        getElement().setProperty("itemLabelPath", itemLabelPath == null ? "" : itemLabelPath);
    }

    private void setItemIdPath(String itemIdPath) {
        getElement().setProperty("itemIdPath", itemIdPath == null ? "" : itemIdPath);
    }

    protected void setItems(JsonArray items) {
        getElement().setPropertyJson("items", items);
    }

    protected void setSelectedItems(JsonArray items) {
        getElement().setPropertyJson("selectedItems", items);
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
        Objects.requireNonNull(itemLabelGenerator, "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        reset();
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "The data provider can not be null");
        this.dataProvider = dataProvider;
        reset();
        updateDataProviderListenerRegistration();
    }

    private void reset() {
        keyMapper.removeAll();
        clear();
        Set<T> data = dataProvider.fetch(new Query<>()).collect(Collectors.toCollection(LinkedHashSet::new));
        setItems(modelToPresentation(this, data));
    }

    private void updateDataProviderListenerRegistration() {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider.addDataProviderListener(e -> reset());
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(
            ui -> ui.beforeClientResponse(this, context -> command.accept(ui)));
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

    private static <T> Set<T> presentationToModel(MultiselectComboBox<T> multiselectComboBox, JsonArray presentation) {
        JsonArray array = presentation;
        Set<T> set = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            String key = array.getObject(i).getString(KEY_ATTRIBUTE);
            set.add(multiselectComboBox.keyMapper.get(key));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(MultiselectComboBox<T> multiselectComboBox, Set<T> model) {
        JsonArray array = Json.createArray();
        if (model.isEmpty()) {
            return array;
        }

        model.stream()
            .map(multiselectComboBox::generateJson)
            .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
    }

    private JsonObject generateJson(T item) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put(KEY_ATTRIBUTE, keyMapper.key(item));
        dataGenerator.generateData(item, jsonObject);
        return jsonObject;
    }

    private Object getItemId(T item) {
        if (getDataProvider() == null) {
            return item;
        }
        return getDataProvider().getId(item);
    }
}
