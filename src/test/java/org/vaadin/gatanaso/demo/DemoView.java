package org.vaadin.gatanaso.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.gatanaso.MultiselectComboBox;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class DemoView extends VerticalLayout {

    public DemoView() {
        setSpacing(true);
        setMargin(true);

        addTitle();
        addSimpleStringDemo();
        addObjectDemo();
        addObjectDemoWithLabelGenerator();
        addRequiredDemo();
        addCompactModeDemo();
        addOrderedDemo();
    }

    private void addTitle() {
        Label title = new Label("Multiselect Combo Box Demos");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);
    }

    private void addSimpleStringDemo() {
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel("Multiselect combo box with string items");
        multiselectComboBox.setPlaceholder("Add");
        multiselectComboBox.setItems("Item 1", "Item 2", "Item 3", "Item 4");
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> multiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void addObjectDemo() {
        MultiselectComboBox<User> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel("Multiselect combo box with object items");
        multiselectComboBox.setPlaceholder("Add");
        List<User> data = Arrays.asList(
                new User("Leanne Graham", "leanne", "leanne@demo.dev"),
                new User("Ervin Howell", "ervin", "ervin@demo.dev"),
                new User("Samantha Doe", "samantha", "samantha@demo.dev"));
        multiselectComboBox.setItems(data);
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> objectMultiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void addObjectDemoWithLabelGenerator() {
        MultiselectComboBox<User> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel(
                "Multiselect combo box with object items and custom item label generator");
        multiselectComboBox.setPlaceholder("Add");
        List<User> data = Arrays.asList(
                new User("Leanne Graham", "leanne", "leanne@demo.dev"),
                new User("Ervin Howell", "ervin", "ervin@demo.dev"),
                new User("Samantha Doe", "samantha", "samantha@demo.dev"));
        multiselectComboBox.setItems(data);
        multiselectComboBox.setItemLabelGenerator(User::getEmail);
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> objectMultiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void addRequiredDemo() {
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel("Required multiselect combo box");
        multiselectComboBox.setPlaceholder("Add");
        multiselectComboBox.setRequired(true);
        multiselectComboBox.setErrorMessage("The field is mandatory");
        multiselectComboBox.setItems("Item 1", "Item 2", "Item 3", "Item 4");
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> multiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void addCompactModeDemo() {
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel("Multiselect combo box in compact mode");
        multiselectComboBox.setPlaceholder("Add");
        multiselectComboBox.setItems("Item 1", "Item 2", "Item 3", "Item 4");
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        multiselectComboBox.setCompactMode(true);

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> multiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void addOrderedDemo() {
        MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox();
        multiselectComboBox.setLabel(
                "Multiselect combo box with ordered selected items list");
        multiselectComboBox.setPlaceholder("Add");
        multiselectComboBox.setItems("Item 1", "Item 2", "Item 3", "Item 4");
        multiselectComboBox.addSelectionListener(
                event -> Notification.show(event.toString()));

        multiselectComboBox.setOrdered(true);

        Button getValueBtn = new Button("Get value");
        getValueBtn.addClickListener(
                event -> multiselectComboBoxValueChangeHandler(
                        multiselectComboBox));

        add(buildDemoContainer(multiselectComboBox, getValueBtn));
    }

    private void multiselectComboBoxValueChangeHandler(
            MultiselectComboBox<String> multiselectComboBox) {
        Set<String> selectedItems = multiselectComboBox.getValue();
        String value = selectedItems.stream().collect(Collectors.joining(", "));
        Notification.show("Items value: " + value);
    }

    private void objectMultiselectComboBoxValueChangeHandler(
            MultiselectComboBox<User> multiselectComboBox) {
        Set<User> selectedItems = multiselectComboBox.getValue();
        String value = selectedItems.stream().map(User::toString)
                .collect(Collectors.joining(", "));
        Notification.show("Users value: " + value);
    }

    private VerticalLayout buildDemoContainer(
            MultiselectComboBox multiselectComboBox, Button... actions) {
        VerticalLayout demoContainer = new VerticalLayout();
        demoContainer.getStyle().set("background-color", "#fcfcfc");
        demoContainer.getStyle().set("box-shadow", "1px 1px 1px 1px #ccc");
        demoContainer.setSpacing(true);
        demoContainer.setMargin(true);
        demoContainer.setWidth("600px");
        setHorizontalComponentAlignment(Alignment.CENTER, demoContainer);
        demoContainer.add(multiselectComboBox);
        for (Button action : actions) {
            demoContainer.add(action);
        }
        return demoContainer;
    }

    /**
     * Example demo object class.
     */
    class User {
        private String name;
        private String username;
        private String email;

        public User(String name, String username, String email) {
            this.name = name;
            this.username = username;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
