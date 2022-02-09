package org.vaadin.gatanaso;

import java.util.function.Function;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.StateNode;

import elemental.json.JsonArray;

/**
 * Data communicator that handles requesting data and sending it to client side.
 *
 * @param <T> the bean type
 */
public class MultiselectComboBoxDataCommunicator<T> extends DataCommunicator<T> {

	private Function<T, Object> uniqueKeyDataGenerator = Object::hashCode;

	private KeyMapper<T> uniqueKeyMapper = new KeyMapper<T>() {

		private T object;

		@Override
		public String key(T o) {
			this.object = o;
			try {
				return super.key(o);
			} finally {
				this.object = null;
			}
		}

		@Override
		protected String createKey() {
			return String.valueOf(uniqueKeyDataGenerator.apply(object));
		}
	};

	/**
	 * Creates a new instance.
	 *
	 * @param dataGenerator
	 *            the data generator function
	 * @param arrayUpdater
	 *            array updater strategy
	 * @param dataUpdater
	 *            data updater strategy
	 * @param stateNode
	 *            the state node used to communicate for
	 */
	public MultiselectComboBoxDataCommunicator(DataGenerator<T> dataGenerator, ArrayUpdater arrayUpdater, SerializableConsumer<JsonArray> dataUpdater,
			StateNode stateNode) {

		super(dataGenerator, arrayUpdater, dataUpdater, stateNode);
		setKeyMapper(uniqueKeyMapper);
	}

	/**
	 * Sets the given {@link Function} as unique key data generator.
	 * The default implementation is {@link Object#hashCode()}.
	 *
	 * @param uniqueKeyDataGenerator {@link Function} to generate unique key data
	 */
	public void setUniqueKeyDataGenerator(Function<T, Object> uniqueKeyDataGenerator) {
		this.uniqueKeyDataGenerator = uniqueKeyDataGenerator;
	}
}
