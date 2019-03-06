package org.vaadin.gatanaso.tests;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import org.junit.Assert;
import org.junit.Test;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for the {@link MultiselectComboBox}.
 */
public class MultiselectComboBoxTest {

    @Test
    public void shouldVerifyItemLabelPath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        Assert.assertEquals(MultiselectComboBox.ITEM_LABEL_PATH, multiselectComboBox.getElement().getProperty("itemLabelPath"));
    }

    @Test
    public void shouldVerifyItemValuePath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        Assert.assertEquals(MultiselectComboBox.ITEM_VALUE_PATH, multiselectComboBox.getElement().getProperty("itemValuePath"));
    }

    @Test
    public void shouldVerifyItemIdPath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        Assert.assertEquals(MultiselectComboBox.ITEM_VALUE_PATH, multiselectComboBox.getElement().getProperty("itemIdPath"));
    }

    @Test
    public void shouldSetLabel() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String label = "Multiselect combo box";

        // when
        multiselectComboBox.setLabel(label);

        // then
        Assert.assertEquals(multiselectComboBox.getLabel(), label);
    }

    @Test
    public void shouldSetPlaceholder() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String placeholder = "Add items";

        // when
        multiselectComboBox.setPlaceholder(placeholder);

        // then
        Assert.assertEquals(multiselectComboBox.getPlaceholder(), placeholder);
    }

    @Test
    public void shouldSetRequired() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isRequired());

        // when
        multiselectComboBox.setRequired(true);

        // then
        Assert.assertTrue(multiselectComboBox.isRequired());
        Assert.assertEquals(Boolean.TRUE.toString(), multiselectComboBox.getElement().getProperty("required"));
    }

    @Test
    public void shouldSetReadOnly() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isReadOnly());

        // when
        multiselectComboBox.setReadOnly(true);

        // then
        Assert.assertTrue(multiselectComboBox.isReadOnly());
        Assert.assertEquals(Boolean.TRUE.toString(), multiselectComboBox.getElement().getProperty("readonly"));
    }

    @Test
    public void shouldSetInvalid() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isInvalid());

        // when
        multiselectComboBox.setInvalid(true);

        // then
        Assert.assertTrue(multiselectComboBox.isInvalid());
        Assert.assertEquals(Boolean.TRUE.toString(), multiselectComboBox.getElement().getProperty("invalid"));
    }

    @Test
    public void shouldSetErrorMessage() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String message = "error message";

        // when
        multiselectComboBox.setErrorMessage(message);

        // then
        Assert.assertEquals(multiselectComboBox.getErrorMessage(), message);
    }

    @Test
    public void shouldSetItems() {
        // given
        TestMultiselectComboBox multiselectComboBox = new TestMultiselectComboBox();

        // when
        multiselectComboBox.setItems(Arrays.asList("item 1", "item 2", "item 3"));

        // then
        Assert.assertEquals(3, multiselectComboBox.items.size());
        Assert.assertEquals(multiselectComboBox.items.get(0), "item 1");
        Assert.assertEquals(multiselectComboBox.items.get(1), "item 2");
        Assert.assertEquals(multiselectComboBox.items.get(2), "item 3");
    }

    private static class TestMultiselectComboBox extends MultiselectComboBox<String> {

        private List<String> items;

        @Override
        public void setDataProvider(DataProvider<String, ?> dataProvider) {
            super.setDataProvider(dataProvider);
            items = dataProvider.fetch(new Query<>()).collect(Collectors.toList());
        }
    }

    @Test
    public void shouldUpdateDataProviderAndResetValueToEmpty() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();
        multiselectComboBox.setItems(Arrays.asList("item 1", "item 2")); // data provider is set

        // when
        Set<Object> value = Arrays.asList("item 1").stream().collect(Collectors.toCollection(LinkedHashSet::new));
        multiselectComboBox.setValue(value);

        Assert.assertEquals("item 1", multiselectComboBox.getValue().toArray()[0]); // ensure value is set

        multiselectComboBox.setItems(Arrays.asList("foo", "bar")); // update data provider

        // then
        Assert.assertEquals(0, multiselectComboBox.getValue().size()); // value is reset to empty
    }
}
