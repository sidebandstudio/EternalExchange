package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.compat.events.ItemAttributeModifierEvent;

public interface IHasConditionalAttributes {

	/**
	 * Called on any items that implement this interface when the event is fired for an item of that type.
	 */
	void adjustAttributes(ItemAttributeModifierEvent event);
}