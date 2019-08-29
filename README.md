[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/multiselect-combo-box)
[![Build Status](https://travis-ci.org/gatanaso/multiselect-combo-box-flow.svg?branch=master)](https://travis-ci.org/gatanaso/multiselect-combo-box-flow)
[![Version on Vaadin Directory](http://img.shields.io/vaadin-directory/version/multiselect-combo-box.svg)](https://vaadin.com/directory/component/multiselect-combo-box)
[![Stars on vaadin.com/directory](https://img.shields.io/vaadin-directory/star/multiselect-combo-box.svg)](https://vaadin.com/directory/component/multiselect-combo-box)

# MultiselectComboBox

A multi select combo box component for Vaadin Flow.

Integration of of the [multiselect-combo-box](https://github.com/gatanaso/multiselect-combo-box) web component.

#### [Live Demo ↗](https://multiselect-combo-box-flow.herokuapp.com/)

## Install

Add the `multiselect-combo-box-flow dependency` to your `pom.xml` file:
```xml
<dependency>
   <groupId>org.vaadin.gatanaso</groupId>
   <artifactId>multiselect-combo-box-flow</artifactId>
   <version>2.0.0</version>
</dependency>
```

Add the `vaadin-addons` repository:
```xml
<repository>
   <id>vaadin-addons</id>
   <url>http://maven.vaadin.com/vaadin-addons</url>
</repository>
```

## Basic Usage

Create a `MultiselectComboBox` and add items
```java
MultiselectComboBox<String> multiselectComboBox = new MultiselectComboBox();

multiselectComboBox.setLabel("Select items");

multiselectComboBox.setItems("Item 1", "Item 2", "Item 3", "Item 4");
```

Add a value change listener (invoked when the selected items/value is changed):
```java
multiselectComboBox.addValueChangeListener(event -> {
    // handle value change
});
```

Get the selected items/value:
```java
// set of selected values, or an empty set if none selected
Set<String> value = multiselectComboBox.getValue();
```

`MultiselectComboBox` also implements the [MultiSelect](https://vaadin.com/api/platform/12.0.3/com/vaadin/flow/data/selection/MultiSelect.html) interface, 
which makes it easy to listen for selection changes: 
```java
multiselectComboBox.addSelectionListener(event -> {
   event.getAddedSelection(); // get added items
   event.getRemovedSelection() // get removed items
});
```

## Object items

The `MultiselectComboBox` supports object items. Given the following `User` class:
```java
class User {
    private String name;
    private String username;
    private String email;

    public User(String name, String username, String email) {
        this.username = username;
        this.email = email;
    }

    // getters and setters intentionally omitted for brevity

    @Override
    public String toString() {
        return name;
    }
}
```

Create a `MultiselectComboBox` of `User`s:
```java
MultiselectComboBox<User> multiselectComboBox = new MultiselectComboBox();
    
multiselectComboBox.setLabel("Select users");
    
List<User> users = Arrays.asList(
    new User("Leanne Graham","leanne","leanne@demo.dev"),
    new User("Ervin Howell","ervin","ervin@demo.dev"),
    new User("Samantha Doe","samantha","samantha@demo.dev")
);

// by default uses `User.toString()` to generate item labels
multiselectComboBox.setItems(users);
```

The `MultiselectComboBox` uses the `toString()` method to generate the item labels by default. 
This can be overridden by setting an item label generator:
```java
// use the user email as an item label
multiselectComboBox.setItemLabelGenerator(User::getEmail)
```

## Version information
* 2.x.x - the version for Vaadin 14
* 1.x.x. - the version for Vaadin 13 and Vaadin 12

### Vaadin 12 support
To use this component in a Vaadin 12+ project, 
explicitly override the `vaadin-combo-box` dependency version by adding the following to your pom.xml file:
```xml
<dependency>
	<groupId>org.webjars.bowergithub.vaadin</groupId>
	<artifactId>vaadin-combo-box</artifactId>
	<version>4.2.7</version>
</dependency>
```
Optionally, to always use the latest version, a range can be specified as follows:
```xml
<version>[4.2.7, 5)</version>
```

## Branch information
* `master` the latest version for Vaadin 14
* `V13` the version for Vaadin 13 and Vaadin 12

## Running demos locally

1. Fork the `multiselect-combo-box-flow` repository and clone it locally.
1. Build the project: `mvn clean install`
1. Start the test/demo server: `mvn jetty:run`
1. Navigate to http://localhost:8080 to view the demo.

## Web Component
The `<multiselect-combo-box>` web component is available on [webcomponents.org](https://www.webcomponents.org/element/multiselect-combo-box), 
the [Vaadin Directory](https://vaadin.com/directory/component/gatanasomultiselect-combo-box) and [GitHub](https://github.com/gatanaso/multiselect-combo-box).