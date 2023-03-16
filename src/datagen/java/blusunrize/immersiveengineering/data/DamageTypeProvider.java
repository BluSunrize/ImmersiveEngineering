/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeProvider
{
	private static final float DEFAULT_EXHAUSTION = 0.1f;

	public static void bootstrap(BootstapContext<DamageType> ctx)
	{
		ctx.register(Lib.DMG_RevolverCasull, new DamageType("ieRevolver_casull", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverAP, new DamageType("ieRevolver_armorPiercing", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverBuck, new DamageType("ieRevolver_buckshot", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverDragon, new DamageType("ieRevolver_dragonsbreath", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverHoming, new DamageType("ieRevolver_homing", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverWolfpack, new DamageType("ieRevolver_wolfpack", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverSilver, new DamageType("ieRevolver_silver", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RevolverPotion, new DamageType("ieRevolver_potion", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Crusher, new DamageType("ieCrushed", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Sawmill, new DamageType("ieSawmill", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Tesla, new DamageType("ieTesla", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Acid, new DamageType("ieAcid", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Railgun, new DamageType("ieRailgun", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Sawblade, new DamageType("ieSawblade", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_Tesla_prim, new DamageType("ieTeslaPrimary", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RazorWire, new DamageType("ieRazorWire", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_RazorShock, new DamageType("ieRazorShock", DEFAULT_EXHAUSTION));
		ctx.register(Lib.DMG_WireShock, new DamageType("ieWireShock", DEFAULT_EXHAUSTION));
	}
}
