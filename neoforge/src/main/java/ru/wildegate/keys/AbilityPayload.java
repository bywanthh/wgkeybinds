package ru.wildegate.keys;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Пакет C2S канала {@code wildegate:keys}.
 *
 * <p>Содержимое: VarInt — ID: {@code 0} = мини-хендшейк «мод установлен»
 * (способность не вызывается), {@code 1} = Способность №1, {@code 2} = Способность №2.
 * Ровно тот формат, который ждёт серверная часть (плагин wildegate-channel-bridge
 * превращает пакет в команду {@code /wgkey}, её обрабатывает WildeGate_Races.sk).</p>
 */
public record AbilityPayload(int ability) implements CustomPacketPayload {
	/** Мини-хендшейк при входе: сообщает серверу, что у игрока есть мод. */
	public static final int HELLO = 0;
	public static final int ABILITY_1 = 1;
	public static final int ABILITY_2 = 2;

	public static final CustomPacketPayload.Type<AbilityPayload> TYPE =
			new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WildeGateKeys.MOD_ID, "keys"));

	public static final StreamCodec<ByteBuf, AbilityPayload> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, payload -> payload.ability(), AbilityPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
