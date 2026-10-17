package ru.wildegate.keys;

/**
 * Общие константы мода (доступны и клиентскому, и main source set).
 */
public final class WildeGateKeys {
	/** ID мода = namespace всех идентификаторов. */
	public static final String MOD_ID = "wildegate";
	/** Полный идентификатор канала: wildegate:keys */
	public static final String CHANNEL = "wildegate:keys";
	/** Внутренняя команда Skript-моста (используется как резервный путь). */
	public static final String FALLBACK_COMMAND = "wgkey";

	private WildeGateKeys() {
	}
}
