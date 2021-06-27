/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class IEEntityTypes
{
	public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, Lib.MODID);
	public static final RegistryObject<EntityType<ChemthrowerShotEntity>> CHEMTHROWER_SHOT = register(
			"chemthrower_shot",
			() -> Builder.<ChemthrowerShotEntity>create(ChemthrowerShotEntity::new, EntityClassification.MISC)
					.size(0.1F, 0.1F)
	);
	public static final RegistryObject<EntityType<FluorescentTubeEntity>> FLUORESCENT_TUBE = register(
			"fluorescent_tube",
			() -> Builder.<FluorescentTubeEntity>create(FluorescentTubeEntity::new, EntityClassification.MISC)
					.size(FluorescentTubeEntity.TUBE_LENGTH/2, 1+FluorescentTubeEntity.TUBE_LENGTH/2)
	);
	public static final RegistryObject<EntityType<IEExplosiveEntity>> EXPLOSIVE = register(
			"explosive",
			() -> Builder.<IEExplosiveEntity>create(IEExplosiveEntity::new, EntityClassification.MISC)
					.immuneToFire()
					.size(0.98F, 0.98F)
	);
	public static final RegistryObject<EntityType<RailgunShotEntity>> RAILGUN_SHOT = register(
			"railgun_shot",
			() -> Builder.<RailgunShotEntity>create(RailgunShotEntity::new, EntityClassification.MISC)
					.size(.5F, .5F)
	);
	public static final RegistryObject<EntityType<RevolvershotEntity>> REVOLVERSHOT = register(
			"revolver_shot",
			() -> Builder.<RevolvershotEntity>create(RevolvershotEntity::new, EntityClassification.MISC)
					.size(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<RevolvershotFlareEntity>> FLARE_REVOLVERSHOT = register(
			"revolver_shot_flare",
			() -> Builder.<RevolvershotFlareEntity>create(RevolvershotFlareEntity::new, EntityClassification.MISC)
					.size(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<RevolvershotHomingEntity>> HOMING_REVOLVERSHOT = register(
			"revolver_shot_homing",
			() -> Builder.<RevolvershotHomingEntity>create(RevolvershotHomingEntity::new, EntityClassification.MISC)
					.size(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<SkylineHookEntity>> SKYLINE_HOOK = register(
			"skyline_hook",
			() -> Builder.<SkylineHookEntity>create(SkylineHookEntity::new, EntityClassification.MISC)
					.size(.125F, .125F)
	);
	public static final RegistryObject<EntityType<WolfpackShotEntity>> WOLFPACK_SHOT = register(
			"revolver_shot_wolfpack",
			() -> Builder.<WolfpackShotEntity>create(WolfpackShotEntity::new, EntityClassification.MISC)
					.size(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<CrateMinecartEntity>> CRATE_MINECART = register(
			"cart_woodencrate",
			() -> Builder.<CrateMinecartEntity>create(CrateMinecartEntity::new, EntityClassification.MISC)
					.size(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<ReinforcedCrateMinecartEntity>> REINFORCED_CRATE_CART = register(
			"cart_reinforcedcrate",
			() -> Builder.<ReinforcedCrateMinecartEntity>create(ReinforcedCrateMinecartEntity::new, EntityClassification.MISC)
					.size(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<BarrelMinecartEntity>> BARREL_MINECART = register(
			"cart_woodenbarrel",
			() -> Builder.<BarrelMinecartEntity>create(BarrelMinecartEntity::new, EntityClassification.MISC)
					.size(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<MetalBarrelMinecartEntity>> METAL_BARREL_CART = register(
			"cart_metalbarrel",
			() -> Builder.<MetalBarrelMinecartEntity>create(MetalBarrelMinecartEntity::new, EntityClassification.MISC)
					.size(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<SawbladeEntity>> SAWBLADE = register(
			"sawblade",
			() -> Builder.<SawbladeEntity>create(SawbladeEntity::new, EntityClassification.MISC)
					.size(.75F, .2F)
	);

	private static <T extends Entity>
	RegistryObject<EntityType<T>> register(String name, Supplier<Builder<T>> prepare)
	{
		return REGISTER.register(name, () -> prepare.get().build(ImmersiveEngineering.MODID+":"+name));
	}
}
