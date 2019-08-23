package org.vaadin.gatanaso;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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
        assertThat(
                multiselectComboBox.getElement().getProperty("itemLabelPath"),
                is(MultiselectComboBox.ITEM_LABEL_PATH));
    }

    @Test
    public void shouldVerifyItemValuePath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        assertThat(
                multiselectComboBox.getElement().getProperty("itemValuePath"),
                is(MultiselectComboBox.ITEM_VALUE_PATH));
    }

    @Test
    public void shouldVerifyItemIdPath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        assertThat(multiselectComboBox.getElement().getProperty("itemIdPath"),
                is(MultiselectComboBox.ITEM_VALUE_PATH));
    }

    @Test
    public void shouldSetLabel() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String label = "Multiselect combo box";

        // when
        multiselectComboBox.setLabel(label);

        // then
        assertThat(multiselectComboBox.getLabel(), is(label));
    }

    @Test
    public void shouldSetPlaceholder() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String placeholder = "Add items";

        // when
        multiselectComboBox.setPlaceholder(placeholder);

        // then
        assertThat(multiselectComboBox.getPlaceholder(), is(placeholder));
    }

    @Test
    public void shouldSetRequired() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isRequired());

        // when
        multiselectComboBox.setRequired(true);

        // then
        assertThat(multiselectComboBox.isRequired(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("required"),
                is("true"));
    }

    @Test
    public void shouldSetReadOnly() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isReadOnly());

        // when
        multiselectComboBox.setReadOnly(true);

        // then
        assertThat(multiselectComboBox.isReadOnly(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("readonly"),
                is("true"));
    }

    @Test
    public void shouldSetCompactMode() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isCompactMode());

        // when
        multiselectComboBox.setCompactMode(true);

        // then
        assertThat(multiselectComboBox.isCompactMode(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("compactMode"),
                is("true"));
    }

    @Test
    public void shouldSetOrdered() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isOrdered());

        // when
        multiselectComboBox.setOrdered(true);

        // then
        assertThat(multiselectComboBox.isOrdered(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("ordered"),
                is("true"));
    }

    @Test
    public void shouldSetInvalid() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isInvalid());

        // when
        multiselectComboBox.setInvalid(true);

        // then
        assertThat(multiselectComboBox.isInvalid(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("invalid"),
                is("true"));
    }

    @Test
    public void shouldSetErrorMessage() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        String message = "error message";

        // when
        multiselectComboBox.setErrorMessage(message);

        // then
        assertThat(multiselectComboBox.getErrorMessage(), is(message));
    }

    @Test
    public void shouldSetItems() {
        // given
        TestMultiselectComboBox multiselectComboBox = new TestMultiselectComboBox();

        // when
        multiselectComboBox
                .setItems(Arrays.asList("item 1", "item 2", "item 3"));

        // then
        assertThat(multiselectComboBox.items, hasSize(3));
        assertThat(multiselectComboBox.items, hasItem("item 1"));
        assertThat(multiselectComboBox.items, hasItem("item 2"));
        assertThat(multiselectComboBox.items, hasItem("item 3"));
    }

    @Test
    public void shouldUpdateDataProviderAndResetValueToEmpty() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();
        multiselectComboBox.setItems(Arrays.asList("item 1", "item 2")); // data
                                                                         // provider
                                                                         // is
                                                                         // set

        Set<Object> value = new LinkedHashSet<>(Arrays.asList("item 1"));

        // when
        multiselectComboBox.setValue(value);
        assertThat(multiselectComboBox.getValue(), hasItem("item 1")); // ensure
                                                                       // value
                                                                       // is set

        multiselectComboBox.setItems(Arrays.asList("foo", "bar")); // update
                                                                   // data
                                                                   // provider

        // then
        assertThat(multiselectComboBox.getValue(), hasSize(0)); // value is
                                                                // reset to
                                                                // empty
    }

    @Test
    public void shouldApplyDataProviderFilterAndNotResetTheValue() {
        // given
        ConfigurableFilterDataProvider<String, Void, SerializablePredicate<String>> dataProvider = DataProvider
                .ofItems("Java", "Python", "Ruby", "Go")
                .withConfigurableFilter();

        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        multiselectComboBox.setDataProvider(dataProvider);

        multiselectComboBox
                .setValue(new LinkedHashSet<>(Arrays.asList("Python", "Go")));

        assertThat(multiselectComboBox.getValue(), hasItem("Python")); // ensure
                                                                       // value
                                                                       // is set
        assertThat(multiselectComboBox.getValue(), hasItem("Go"));

        // when
        dataProvider.setFilter(d -> !d.equals("Java"));

        // then
        assertThat(multiselectComboBox.getValue(), hasSize(2));
        assertThat(multiselectComboBox.getValue(), hasItem("Python"));
        assertThat(multiselectComboBox.getValue(), hasItem("Go"));
    }

    private static class TestMultiselectComboBox
            extends MultiselectComboBox<String> {

        private List<String> items;

        @Override
        public void setDataProvider(DataProvider<String, ?> dataProvider) {
            super.setDataProvider(dataProvider);
            items = dataProvider.fetch(new Query<>())
                    .collect(Collectors.toList());
        }
    }
}
