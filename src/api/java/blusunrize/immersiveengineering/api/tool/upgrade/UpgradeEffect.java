/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.upgrade;

import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData.UpgradeEntry;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import com.mojang.datafixers.util.Unit;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public record UpgradeEffect<T>(String name, DualCodec<ByteBuf, T> valueCodec, T defaultValue)
{
	private static final Map<String, UpgradeEffect<?>> TYPES = new HashMap<>();

	public static final UpgradeEffect<Unit> WATERPROOF = unit("waterproof");
	public static final UpgradeEffect<Unit> OILED = unit("oiled");
	public static final UpgradeEffect<Unit> FORTUNE = unit("fortune");
	public static final UpgradeEffect<Unit> ELECTRO = unit("electro");
	public static final UpgradeEffect<Unit> FOCUS = unit("focus");
	public static final UpgradeEffect<Unit> SCOPE = unit("scope");
	public static final UpgradeEffect<Cooldown> FLASH = new UpgradeEffect<>("flash", Cooldown.CODECS, Cooldown.IDLE);
	public static final UpgradeEffect<Cooldown> SHOCK = new UpgradeEffect<>("shock", Cooldown.CODECS, Cooldown.IDLE);
	public static final UpgradeEffect<PrevSlot> MAGNET = new UpgradeEffect<>("prevSlot", PrevSlot.CODECS, PrevSlot.NONE);
	public static final UpgradeEffect<Unit> MULTITANK = unit("multitank");
	public static final UpgradeEffect<Unit> SPAREBLADES = unit("spareblades");
	public static final UpgradeEffect<Unit> ANTENNA = unit("antenna");
	public static final UpgradeEffect<Unit> INDUCTION = unit("induction");
	public static final UpgradeEffect<Unit> TESLA = unit("tesla");
	public static final UpgradeEffect<Float> SPEED = new UpgradeEffect<>("speed", DualCodecs.FLOAT, 0f);
	public static final UpgradeEffect<Integer> DAMAGE = new UpgradeEffect<>("damage", DualCodecs.INT, 0);
	public static final UpgradeEffect<Integer> CAPACITY = new UpgradeEffect<>("capacity", DualCodecs.INT, 0);
	public static final UpgradeEffect<Float> MELEE = new UpgradeEffect<>("melee", DualCodecs.FLOAT, 0f);
	public static final UpgradeEffect<Integer> BULLETS = new UpgradeEffect<>("bullets", DualCodecs.INT, 0);
	public static final UpgradeEffect<Float> NOISE = new UpgradeEffect<>("noise", DualCodecs.FLOAT, 1f);
	public static final UpgradeEffect<Float> COOLDOWN = new UpgradeEffect<>("cooldown", DualCodecs.FLOAT, 1f);
	public static final UpgradeEffect<Float> LUCK = new UpgradeEffect<>("luck", DualCodecs.FLOAT, 0f);

	// Revolver "special" effects, only available via contributor JSON
	public static final UpgradeEffect<Unit> NERF = unit("nerf");
	public static final UpgradeEffect<Unit> FANCY_ANIMATION = unit("fancyAnimation");

	private static UpgradeEffect<Unit> unit(String name)
	{
		return new UpgradeEffect<>(name, DualCodecs.unit(Unit.INSTANCE), Unit.INSTANCE);
	}

	public UpgradeEffect
	{
		TYPES.put(name, this);
	}

	public static UpgradeEffect<?> get(String key)
	{
		return TYPES.get(key);
	}

	public DualCodec<ByteBuf, UpgradeEntry<?>> entryCodec()
	{
		return valueCodec.map((T t) -> new UpgradeEntry<>(this, t), upgradeEntry -> (T)upgradeEntry.value());
	}
}
