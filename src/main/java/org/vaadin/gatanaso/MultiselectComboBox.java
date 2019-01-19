package org.vaadin.gatanaso;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasSize;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag("multiselect-combo-box")
@HtmlImport("bower_components/multiselect-combo-box/multiselect-combo-box.html")
public class MultiselectComboBox<T>
        extends AbstractField<MultiselectComboBox<T>, Set<T>>
        implements HasDataProvider<T>, HasSize {

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private DataProvider<T, ?> dataProvider;
    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private Registration dataProviderListenerRegistration;

    public MultiselectComboBox() {
        super(Collections.emptySet());

        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject
                .put("label", generateLabel(item)));

        setItemIdPath("key");
        setItemValuePath("key");
        setItemLabelPath("label");
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
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * <p>
     * Path for the value of the item. If {@code items} is an array of objects,
     * the {@code itemValuePath} is used to fetch the string value for the
     * selected item.
     * </p>
     * <p>
     * The item value is used in the {@code value} property of the combo box, to
     * provide the form value.
     * </p>
     *
     * @param itemValuePath
     *            the String value to set
     */
    public void setItemValuePath(String itemValuePath) {
        getElement().setProperty("itemValuePath",
                itemValuePath == null ? "" : itemValuePath);
    }

    /**
     * <p>
     * Path for the label of the item. If {@code items} is an array of objects,
     * the {@code itemLabelPath} is used to fetch the string value for the label
     * of the selected item.
     * </p>
     * <p>
     * The item label is used in the multislect-combo-box, as the item display
     * value.
     * </p>
     *
     * @param itemLabelPath
     *            the String value to set
     */
    public void setItemLabelPath(String itemLabelPath) {
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

    protected void setSelectedItems(JsonArray items) {
        getElement().setPropertyJson("selectedItems", items);
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
    }

    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    @Override
    protected void setPresentationValue(Set<T> newPresentationValue) {
        setValue(newPresentationValue);
    }

    public void setValue(Set<T> value) {
        Set<T> nullSafeValue = Optional.ofNullable(value)
                .orElse(getEmptyValue());
        setSelectedItems(convertToJsonArray(nullSafeValue.stream()));
        super.setValue(nullSafeValue);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider,
                "The data provider can not be null");
        this.dataProvider = dataProvider;
        dataProviderUpdated();
        setValue(getEmptyValue());
        updateDataProviderListenerRegistration();
    }

    private void updateDataProviderListenerRegistration() {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(e -> dataProviderUpdated());
    }

    private void dataProviderUpdated() {
		runBeforeClientResponse((SerializableConsumer<UI>) ui -> {
			List<T> data = dataProvider.fetch(new Query<>()).collect(Collectors.toList());
			JsonArray items = convertToJsonArray(data.stream());
			setItems(items);
			Set<T> value = getValue();
			setValue(value);
		});
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private JsonArray convertToJsonArray(Stream<T> data) {
        JsonArray jsonArray = Json.createArray();
        data.map(this::generateJson).forEachOrdered(
                jsonObject -> jsonArray.set(jsonArray.length(), jsonObject));
        return jsonArray;
    }

    private JsonObject generateJson(T item) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("key", keyMapper.key(item));
        dataGenerator.generateData(item, jsonObject);
        return jsonObject;
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
}
