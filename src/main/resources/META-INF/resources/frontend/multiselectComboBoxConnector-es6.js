// ES6 connector for the MultiselectComboBox (based on the ComboBox connector)

import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut } from '@polymer/polymer/lib/utils/async.js';
import './multiselectComboBoxConnector.js';

window.Vaadin.Flow.Legacy.Debouncer = Debouncer;
window.Vaadin.Flow.Legacy.timeOut = timeOut;
