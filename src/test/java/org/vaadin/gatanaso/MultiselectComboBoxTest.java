package org.vaadin.gatanaso;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the {@link MultiselectComboBox}.
 */
public class MultiselectComboBoxTest {

    @Test
    public void shouldInstantiateWithLabel() {
        // given
        MultiselectComboBox<String> multiselectComboBox;
        String label = "Label";

        // when
        multiselectComboBox = new MultiselectComboBox<>(label);

        // then
        assertThat(multiselectComboBox.getLabel(), is(label));
    }

    @Test
    public void shouldInstantiateWithLabelAndCollectionOfItems() {
        // given
        TestMultiselectComboBox<String> multiselectComboBox;
        String label = "Label";
        List<String> items = Arrays.asList("Item 1", "Item 2");

        // when
        multiselectComboBox = new TestMultiselectComboBox<String>(label, items);

        // then
        assertThat(multiselectComboBox.getLabel(), is(label));
        assertThat(multiselectComboBox.items, hasSize(2));
        assertThat(multiselectComboBox.items, hasItem("Item 1"));
        assertThat(multiselectComboBox.items, hasItem("Item 2"));
    }

    @Test
    public void shouldInstantiateWithLabelAndItems() {
        // given
        TestMultiselectComboBox<String> multiselectComboBox;
        String label = "Label";

        // when
        multiselectComboBox = new TestMultiselectComboBox<String>(label, "Item 1", "Item 2");

        // then
        assertThat(multiselectComboBox.getLabel(), is(label));
        assertThat(multiselectComboBox.items, hasSize(2));
        assertThat(multiselectComboBox.items, hasItem("Item 1"));
        assertThat(multiselectComboBox.items, hasItem("Item 2"));
    }

    @Test
    public void shouldVerifyItemLabelPath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        assertThat(multiselectComboBox.getElement().getProperty("itemLabelPath"), is(MultiselectComboBox.ITEM_LABEL_PATH));
    }

    @Test
    public void shouldVerifyItemValuePath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        assertThat(multiselectComboBox.getElement().getProperty("itemValuePath"), is(MultiselectComboBox.ITEM_VALUE_PATH));
    }

    @Test
    public void shouldVerifyItemIdPath() {
        // given
        MultiselectComboBox<String> multiselectComboBox;

        // when
        multiselectComboBox = new MultiselectComboBox<>();

        // then
        assertThat(multiselectComboBox.getElement().getProperty("itemIdPath"), is(MultiselectComboBox.ITEM_VALUE_PATH));
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
        assertThat(multiselectComboBox.getElement().getProperty("required"), is("true"));
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
        assertThat(multiselectComboBox.getElement().getProperty("readonly"), is("true"));
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
        assertThat(multiselectComboBox.getElement().getProperty("compactMode"), is("true"));
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
        assertThat(multiselectComboBox.getElement().getProperty("ordered"), is("true"));
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
        assertThat(multiselectComboBox.getElement().getProperty("invalid"), is("true"));
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
        TestMultiselectComboBox<String> multiselectComboBox = new TestMultiselectComboBox();

        // when
        multiselectComboBox.setItems(Arrays.asList("item 1", "item 2", "item 3"));

        // then
        assertThat(multiselectComboBox.items, hasSize(3));
        assertThat(multiselectComboBox.items, hasItem("item 1"));
        assertThat(multiselectComboBox.items, hasItem("item 2"));
        assertThat(multiselectComboBox.items, hasItem("item 3"));
    }

    @Test
    public void shouldSetPageSize() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        assertThat(multiselectComboBox.getPageSize(), is(50)); // default value

        // when
        multiselectComboBox.setPageSize(10);

        // then
        assertThat(multiselectComboBox.getPageSize(), is(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingInvalidPageSize() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        // when & then
        multiselectComboBox.setPageSize(0);
    }

    @Test
    public void shouldSetClearButtonVisible() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isClearButtonVisible());

        // when
        multiselectComboBox.setClearButtonVisible(true);

        // then
        assertThat(multiselectComboBox.isClearButtonVisible(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("clearButtonVisible"), is("true"));
    }

    @Test
    public void shouldSetReadOnlyValueSeparator() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        // when
        multiselectComboBox.setReadOnlyValueSeparator("***");

        // then
        assertThat(multiselectComboBox.getReadOnlyValueSeparator(), is("***"));
    }

    @Test
    public void shouldSetAllowCustomValues() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Assert.assertFalse(multiselectComboBox.isAllowCustomValues());

        // when
        multiselectComboBox.setAllowCustomValues(true);

        // then
        assertThat(multiselectComboBox.isAllowCustomValues(), is(true));
        assertThat(multiselectComboBox.getElement().getProperty("allowCustomValues"), is("true"));
    }

    @Test
    public void shouldUpdateDataProviderAndResetValueToEmpty() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();
        multiselectComboBox.setItems(Arrays.asList("item 1", "item 2")); // data provider is set

        Set<Object> value = new LinkedHashSet<>(Arrays.asList("item 1"));

        // when
        multiselectComboBox.setValue(value);
        assertThat(multiselectComboBox.getValue(), hasItem("item 1")); // ensure value is set

        multiselectComboBox.setItems(Arrays.asList("foo", "bar")); // update data provider

        // then
        assertThat(multiselectComboBox.getValue(), hasSize(0)); // value is reset to empty
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenSettingNullDataProvider() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();
        DataProvider<Object, String> dataProvider = null;

        // when
        multiselectComboBox.setDataProvider(dataProvider);

        // then, expect exception
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenSettingNullItemLabelGenerator() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();

        // when
        multiselectComboBox.setItemLabelGenerator(null);

        // then, expect exception
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenSettingNullRenderer() {
        // given
        MultiselectComboBox<Object> multiselectComboBox = new MultiselectComboBox<>();

        // when
        multiselectComboBox.setRenderer(null);

        // then, expect exception
    }

    @Test
    public void shouldNotifyValueChangeListener() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();
        multiselectComboBox.setItems("Item 1", "Item 2", "Item 3");

        AtomicReference<Set<String>> selected = new AtomicReference<>();
        multiselectComboBox.addValueChangeListener(event -> selected.set(multiselectComboBox.getValue()));

        // when
        Set<String> value = new LinkedHashSet<>(Arrays.asList("Item 1, Item 2"));
        multiselectComboBox.setValue(value);

        // then
        assertThat(selected.get(), is(value));
    }

    @Test
    public void shouldSetEnabled() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        // when & then
        multiselectComboBox.setEnabled(true);
        assertThat(multiselectComboBox.isEnabled(), is(true));

        multiselectComboBox.setEnabled(false);
        assertThat(multiselectComboBox.isEnabled(), is(false));
    }

    @Test
    public void shouldSetCustomValuesAllowedFlagWhenEventListenerIsRegistered() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        // when
        multiselectComboBox.addCustomValuesSetListener(event -> {
           // no op
        });

        // then
        assertThat(multiselectComboBox.isAllowCustomValues(), is(true));
    }

    @Test
    public void shouldRemoveCustomValuesAllowedFlagWhenEventListenerIsRemoved() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Registration registration = multiselectComboBox.addCustomValuesSetListener(event -> {
            // no op
        });

        // when
        registration.remove();

        // then
        assertThat(multiselectComboBox.isAllowCustomValues(), is(false));
    }

    @Test
    public void shouldRemoveCustomValuesAllowedFlagWhenEventAllListenerAreRemoved() {
        // given
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox<>();

        Registration registration1 = multiselectComboBox.addCustomValuesSetListener(event -> {
            // no op
        });
        Registration registration2 = multiselectComboBox.addCustomValuesSetListener(event -> {
            // no op
        });

        assertThat(multiselectComboBox.isAllowCustomValues(), is(true));

        // when
        registration1.remove();
        registration2.remove();

        // then
        assertThat(multiselectComboBox.isAllowCustomValues(), is(false));
    }

    private static class TestMultiselectComboBox<T> extends MultiselectComboBox<T> {
        private List<T> items;

        public TestMultiselectComboBox() {
            super();
        }

        public TestMultiselectComboBox(String label, Collection<T> items) {
            super(label, items);
        }

        public TestMultiselectComboBox(String label, T... items) {
            super(label, items);
        }

        @Override
        public void setDataProvider(ListDataProvider<T> listDataProvider) {
            super.setDataProvider(listDataProvider);
            items = listDataProvider.fetch(new Query<>())
                    .collect(Collectors.toList());
        }
    }
}
