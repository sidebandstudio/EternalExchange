package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import moze_intel.projecte.compat.network.PacketDistributor;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ClientKeyHelper {

	private static ImmutableBiMap<PEKeybind, KeyMapping> peToMc = ImmutableBiMap.of();

	/**
	 * Fabric port: registered via Fabric's KeyBindingHelper from the client entrypoint. Fabric has no key modifier system, so the shift-modified helmet toggle
	 * shares X with the boots toggle and the two are disambiguated by the shift state at press time.
	 */
	public static void registerKeyBindings() {
		ImmutableBiMap.Builder<PEKeybind, KeyMapping> builder = ImmutableBiMap.builder();
		addKeyBinding(builder, PEKeybind.HELMET_TOGGLE, true, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.BOOTS_TOGGLE, false, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.CHARGE, null, GLFW.GLFW_KEY_V);
		addKeyBinding(builder, PEKeybind.EXTRA_FUNCTION, null, GLFW.GLFW_KEY_C);
		addKeyBinding(builder, PEKeybind.FIRE_PROJECTILE, null, GLFW.GLFW_KEY_R);
		addKeyBinding(builder, PEKeybind.MODE, null, GLFW.GLFW_KEY_G);
		peToMc = builder.build();
	}

	private static void addKeyBinding(ImmutableBiMap.Builder<PEKeybind, KeyMapping> builder, PEKeybind keyBind, @Nullable Boolean requireShift, int keyCode) {
		KeyMapping keyMapping = new PEKeyMapping(keyBind, requireShift, keyCode);
		builder.put(keyBind, keyMapping);
		KeyBindingHelper.registerKeyBinding(keyMapping);
	}

	public static Component getKeyName(PEKeybind k) {
		KeyMapping keyMapping = peToMc.get(k);
		if (keyMapping == null) {
			//Fallback to the translation key of the key's function
			return TextComponentUtil.build(k);
		}
		return keyMapping.getTranslatedKeyMessage();
	}

	private static class PEKeyMapping extends KeyMapping {

		private final PEKeybind keybind;
		@Nullable
		private final Boolean requireShift;
		private boolean lastState;

		PEKeyMapping(PEKeybind keybind, @Nullable Boolean requireShift, int keyCode) {
			super(keybind.getTranslationKey(), InputConstants.Type.KEYSYM, keyCode, PELang.PROJECTE.getTranslationKey());
			this.keybind = keybind;
			this.requireShift = requireShift;
		}

		@Override
		public void setDown(boolean value) {
			super.setDown(value);
			//Note: We check the state based on isDown instead of value, as the value may be wrong depending on the conflict context
			boolean state = isDown();
			if (state != lastState) {
				if (state && (requireShift == null || requireShift == Screen.hasShiftDown())) {
					PacketDistributor.sendToServer(new KeyPressPKT(keybind));
				}
				lastState = state;
			}
		}
	}
}
