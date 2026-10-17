package ru.wildegate.keys;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Общая точка входа мода и его константы.
 */
@Mod(WildeGateKeys.MOD_ID)
public final class WildeGateKeys {
	/** ID мода = namespace всех идентификаторов. */
	public static final String MOD_ID = "wildegate";
	/** Полный идентификатор канала: wildegate:keys */
	public static final String CHANNEL = "wildegate:keys";
	/** Внутренняя команда Skript-моста (резервный путь доставки). */
	public static final String FALLBACK_COMMAND = "wgkey";

	public static final Logger LOGGER = LogUtils.getLogger();

	public WildeGateKeys() {
		LOGGER.info("WildeGate Keys: загружено, канал {}.", CHANNEL);
	}
}
