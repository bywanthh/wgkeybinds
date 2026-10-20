package ru.wildegate.keys;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Клиентская часть мода (только клиент: {@code dist = Dist.CLIENT}).
 *
 * <ul>
 *   <li>Регистрирует категорию «WildeGate: Способности» и два кейбинда: R и G.</li>
 *   <li>Раз в клиентский тик проверяет нажатия (consumeClick).</li>
 *   <li>Отправляет {@code wildegate:keys} (VarInt 1/2) и дублирующую внутреннюю
 *       команду {@code /wgkey <ник> <1|2>} — повторы отсекает анти-дубликат
 *       WildeGate_Races.sk.</li>
 *   <li>При входе в мир отправляет мини-хендшейк (VarInt 0): по нему серверная часть
 *       сразу отключает старые жесты (двойной F / Shift+F).</li>
 * </ul>
 */
@Mod(value = WildeGateKeys.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = WildeGateKeys.MOD_ID, value = Dist.CLIENT)
public final class WildeGateKeysClient {
	// Категория в «Настройки -> Управление -> Назначение клавиш».
	// Ключ локализации: key.category.wildegate.abilities
	private static final KeyMapping.Category CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(WildeGateKeys.MOD_ID, "abilities"));

	private static KeyMapping abilityOne;
	private static KeyMapping abilityTwo;

	/** Мини-хендшейк: -1 = не в мире, >0 = тиков до следующей попытки, 0 = отправляем сейчас. */
	private static int helloDelay = -1;
	private static int helloAttempts = 0;

	@SubscribeEvent
	static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY);

		abilityOne = new KeyMapping("key.wildegate.ability1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
		abilityTwo = new KeyMapping("key.wildegate.ability2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

		event.register(abilityOne);
		event.register(abilityTwo);
	}

	@SubscribeEvent
	static void registerPayloads(RegisterPayloadHandlersEvent event) {
		// Мод клиентский: серверный обработчик не выполняется никогда.
		// Важна только регистрация кодека, чтобы клиент имел право отправить пакет.
		event.registrar("1").optional().playToServer(AbilityPayload.TYPE, AbilityPayload.CODEC, (payload, context) -> {
		});
	}

	@SubscribeEvent
	static void onClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();

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
	 * Мини-хендшейк при входе в мир: пакет с ID 0 сообщает серверу, что у игрока есть мод.
	 * Серверная часть по нему помечает игрока и отключает старые жесты.
	 *
	 * <p>NeoForge не даёт клиенту узнать заранее, объявил ли сервер канал, поэтому
	 * отправка идёт «best effort». Дополнительно, если сервер знает команду
	 * {@code /wgkey} (сервер WildeGate), сразу отправляется командный хендшейк:
	 * мод отмечается при входе даже без плагина-моста. На чужих серверах команды
	 * нет — там мод молчит.</p>
	 */
	private static void tickHello(Minecraft client) {
		if (client.player == null || client.level == null) {
			// Мы не в мире (меню/дисконнект): на следующем входе поздороваемся снова.
			helloDelay = -1;
			helloAttempts = 0;
			return;
		}
		if (helloDelay < 0) {
			helloDelay = 10; // ~0.5 секунды после входа
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

		try {
			ClientPacketDistributor.sendToServer(new AbilityPayload(AbilityPayload.HELLO));
		} catch (Throwable ignored) {
			// сервер не знает канал — сработает командный путь ниже
		}

		if (serverKnowsWgKey(client)) {
			sendFallbackCommand(client, AbilityPayload.HELLO);
			helloAttempts = 5; // одной попытки достаточно
		}
	}

	/**
	 * Есть ли у сервера команда {@code /wgkey} — признак сервера WildeGate
	 * (команда регистрируется скриптом). Дерево команд приходит клиенту при входе.
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

	private static void press(Minecraft client, int ability) {
		AbilityPayload payload = new AbilityPayload(ability);

		// У NeoForge нет аналога Fabric-овского ClientPlayNetworking#canSend, поэтому
		// пакет отправляется «best effort», а команда-дубликат гарантирует доставку.
		// Повторы в пределах нескольких тиков отсекает анти-дубликат WildeGate_Races.sk.
		try {
			ClientPacketDistributor.sendToServer(payload);
		} catch (Throwable ignored) {
			// соединение не поддерживает пакет — сработает командный путь ниже
		}

		sendFallbackCommand(client, ability);
	}

	/** Резервная (невидимая игроку) команда {@code /wgkey <ник> <ID>}. */
	private static void sendFallbackCommand(Minecraft client, int ability) {
		if (client.getConnection() == null) {
			return;
		}
		String name = client.getUser().getName();
		client.getConnection().sendCommand(WildeGateKeys.FALLBACK_COMMAND + " " + name + " " + ability);
	}
}
