package org.vaadin.gatanaso;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route("")
public class DemoView extends Div {

    public DemoView() {
        MultiselectComboBox multiselectComboBox = new MultiselectComboBox();
        
        multiselectComboBox.setLabel("Multiselect combo box");
        multiselectComboBox.setPlaceholder("Add...");
        multiselectComboBox.setRequired(true);
        multiselectComboBox.setErrorMessage("The field is mandatory");

        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4");
        multiselectComboBox.setItems(items);

		add(multiselectComboBox);
    }
}
