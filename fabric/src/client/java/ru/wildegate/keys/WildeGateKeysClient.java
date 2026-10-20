package ru.wildegate.keys;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Точка входа клиентского мода.
 *
 * <ul>
 *   <li>Регистрирует категорию «WildeGate: Способности» и два кейбинда: R и G.</li>
 *   <li>Раз в клиентский тик проверяет нажатия (consumeClick).</li>
 *   <li>Отправляет {@code wildegate:keys} (VarInt 1/2) — но только если сервер объявил,
 *       что принимает этот канал (canSend).</li>
 *   <li>Если сервер канал не объявил (мост-плагин не установлен), используется
 *       командный резервный путь: {@code /wgkey <ник> <1|2>}.</li>
 *   <li>При входе в мир отправляет мини-хендшейк (VarInt 0): по нему серверная часть
 *       сразу отключает старые жесты (двойной F / Shift+F) — они нужны только
 *       игрокам без мода. Если моста нет, но сервер знает {@code /wgkey}, хендшейк
 *       уходит этой же командой.</li>
 * </ul>
 */
public class WildeGateKeysClient implements ClientModInitializer {
	private static KeyMapping abilityOne;
	private static KeyMapping abilityTwo;

	/** Мини-хендшейк: -1 = не в мире, >0 = тиков до следующей попытки, 0 = отправляем сейчас. */
	private static int helloDelay = -1;
	private static int helloAttempts = 0;

	@Override
	public void onInitializeClient() {
		// Кодек пакета обязан быть известен клиенту до первой отправки.
		PayloadTypeRegistry.playC2S().register(AbilityPayload.TYPE, AbilityPayload.CODEC);

		// Категория в «Настройки -> Управление -> Назначение клавиш».
		// Ключ локализации: key.category.wildegate.abilities
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(WildeGateKeys.MOD_ID, "abilities"));

		abilityOne = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.wildegate.ability1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, category));
		abilityTwo = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.wildegate.ability2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
	}

	private void onEndClientTick(Minecraft client) {
		// Хендшейк отправляется до проверок: он же и сбрасывает состояние при выходе.
		tickHello(client);

		// Без подключения (главное меню, одиночная игра, дисконнект) ничего не делаем.
		if (client.player == null) {
			return;
		}
		if (client.getConnection() == null) {
			return;
		}

		while (abilityOne.consumeClick()) {
			press(client, AbilityPayload.ABILITY_1);
		}
		while (abilityTwo.consumeClick()) {
			press(client, AbilityPayload.ABILITY_2);
		}
	}

	/**
	 * Мини-хендшейк при входе в мир: один пакет с ID 0 сообщает серверу, что у игрока
	 * установлен мод. Серверная часть по нему помечает игрока и сразу отключает
	 * старые жесты — остаётся только работа по биндам.
	 *
	 * <p>Если сервер канал не объявил (мост-плагин не установлен), но знает
	 * команду {@code /wgkey} (сервер WildeGate), здороваемся той же командой —
	 * сервер отметит мод сразу при входе, ещё до первого нажатия бинда. Если
	 * команды нет (чужой сервер), молчим: никаких ошибок в чате.</p>
	 */
	private void tickHello(Minecraft client) {
		if (client.player == null || client.level == null) {
			// Мы не в мире (меню/дисконнект): на следующем входе поздороваемся снова.
			helloDelay = -1;
			helloAttempts = 0;
			return;
		}
		if (helloDelay < 0) {
			helloDelay = 10; // ~0.5 секунды после входа: канал сервера уже известен
			return;
		}
		if (helloDelay > 0) {
			helloDelay--;
			return;
		}
		if (helloAttempts >= 5) {
			return;
		}
		helloAttempts++;
		helloDelay = 20; // не чаще раза в секунду

		if (ClientPlayNetworking.canSend(AbilityPayload.TYPE)) {
			ClientPlayNetworking.send(new AbilityPayload(AbilityPayload.HELLO));
			helloAttempts = 5; // одного пакета достаточно: TCP доставит его один раз
			return;
		}

		// Моста нет, но сервер WildeGate знает /wgkey: командный хендшейк.
		if (serverKnowsWgKey(client)) {
			sendFallbackCommand(client, AbilityPayload.HELLO);
			helloAttempts = 5; // одной попытки достаточно
		}
	}

	/**
	 * Есть ли у сервера команда {@code /wgkey} — признак того, что это сервер
	 * WildeGate (команда регистрируется скриптом). Дерево команд приходит клиенту
	 * при входе, поэтому проверка точная и не создаёт ошибок в чате.
	 */
	private static boolean serverKnowsWgKey(Minecraft client) {
		ClientPacketListener connection = client.getConnection();
		if (connection == null) {
			return false;
		}
		CommandDispatcher<ClientSuggestionProvider> commands = connection.getCommands();
		if (commands == null) {
			return false;
		}
		CommandNode<ClientSuggestionProvider> root = commands.getRoot();
		return root.getChild(WildeGateKeys.FALLBACK_COMMAND) != null;
	}

	private void press(Minecraft client, int ability) {
		AbilityPayload payload = new AbilityPayload(ability);

		// Оба способа должны попадать в один серверный шлюз, поэтому содержимое
		// пакета и резервной команды совпадает: только ID способности.
		if (ClientPlayNetworking.canSend(AbilityPayload.TYPE)) {
			ClientPlayNetworking.send(payload);
			return;
		}

		sendFallbackCommand(client, ability);
	}

	/**
	 * Резервный путь для сервера без мост-плагина: обычная (невидимая игроку) команда.
	 * Обрабатывается тем же диспетчером Skript, что и пакет.
	 */
	private void sendFallbackCommand(Minecraft client, int ability) {
		if (client.getConnection() == null) {
			return;
		}
		String name = client.getUser().getName();
		client.getConnection().sendCommand(WildeGateKeys.FALLBACK_COMMAND + " " + name + " " + ability);
	}
}
