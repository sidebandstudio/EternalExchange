package moze_intel.projecte.compat.events;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Fabric port shim: NeoForge fires this event to compute per-stack attribute modifiers dynamically. Fabric has no such hook, so this builds an
 * {@link ItemAttributeModifiers} component instead; call {@link #apply()} after populating to write it to the stack. Items must invoke this when their
 * state (charge/mode) changes rather than relying on an event.
 */
public class ItemAttributeModifierEvent {

	private final ItemStack stack;
	private ItemAttributeModifiers modifiers;
	private boolean changed;

	public ItemAttributeModifierEvent(ItemStack stack) {
		this.stack = stack;
		this.modifiers = stack.getItem().components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
	}

	public ItemStack getItemStack() {
		return stack;
	}

	public void addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slotGroup) {
		modifiers = modifiers.withModifierAdded(attribute, modifier, slotGroup);
		changed = true;
	}

	/**
	 * Writes the accumulated modifiers to the stack's ATTRIBUTE_MODIFIERS component.
	 */
	public void apply() {
		if (changed) {
			stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
		} else if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
			//Reset to item defaults when no conditional modifiers apply anymore
			stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
		}
	}
}
