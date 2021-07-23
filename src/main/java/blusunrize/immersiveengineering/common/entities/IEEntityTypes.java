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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class IEEntityTypes
{
	public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, Lib.MODID);
	public static final RegistryObject<EntityType<ChemthrowerShotEntity>> CHEMTHROWER_SHOT = register(
			"chemthrower_shot",
			() -> Builder.<ChemthrowerShotEntity>of(ChemthrowerShotEntity::new, MobCategory.MISC)
					.sized(0.1F, 0.1F)
	);
	public static final RegistryObject<EntityType<FluorescentTubeEntity>> FLUORESCENT_TUBE = register(
			"fluorescent_tube",
			() -> Builder.<FluorescentTubeEntity>of(FluorescentTubeEntity::new, MobCategory.MISC)
					.sized(FluorescentTubeEntity.TUBE_LENGTH/2, 1+FluorescentTubeEntity.TUBE_LENGTH/2)
	);
	public static final RegistryObject<EntityType<IEExplosiveEntity>> EXPLOSIVE = register(
			"explosive",
			() -> Builder.<IEExplosiveEntity>of(IEExplosiveEntity::new, MobCategory.MISC)
					.fireImmune()
					.sized(0.98F, 0.98F)
	);
	public static final RegistryObject<EntityType<RailgunShotEntity>> RAILGUN_SHOT = register(
			"railgun_shot",
			() -> Builder.<RailgunShotEntity>of(RailgunShotEntity::new, MobCategory.MISC)
					.sized(.5F, .5F)
	);
	public static final RegistryObject<EntityType<RevolvershotEntity>> REVOLVERSHOT = register(
			"revolver_shot",
			() -> Builder.<RevolvershotEntity>of(RevolvershotEntity::new, MobCategory.MISC)
					.sized(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<RevolvershotFlareEntity>> FLARE_REVOLVERSHOT = register(
			"revolver_shot_flare",
			() -> Builder.<RevolvershotFlareEntity>of(RevolvershotFlareEntity::new, MobCategory.MISC)
					.sized(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<RevolvershotHomingEntity>> HOMING_REVOLVERSHOT = register(
			"revolver_shot_homing",
			() -> Builder.<RevolvershotHomingEntity>of(RevolvershotHomingEntity::new, MobCategory.MISC)
					.sized(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<SkylineHookEntity>> SKYLINE_HOOK = register(
			"skyline_hook",
			() -> Builder.<SkylineHookEntity>of(SkylineHookEntity::new, MobCategory.MISC)
					.sized(.125F, .125F)
	);
	public static final RegistryObject<EntityType<WolfpackShotEntity>> WOLFPACK_SHOT = register(
			"revolver_shot_wolfpack",
			() -> Builder.<WolfpackShotEntity>of(WolfpackShotEntity::new, MobCategory.MISC)
					.sized(0.125f, 0.125f)
	);
	public static final RegistryObject<EntityType<CrateMinecartEntity>> CRATE_MINECART = register(
			"cart_woodencrate",
			() -> Builder.<CrateMinecartEntity>of(CrateMinecartEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<ReinforcedCrateMinecartEntity>> REINFORCED_CRATE_CART = register(
			"cart_reinforcedcrate",
			() -> Builder.<ReinforcedCrateMinecartEntity>of(ReinforcedCrateMinecartEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<BarrelMinecartEntity>> BARREL_MINECART = register(
			"cart_woodenbarrel",
			() -> Builder.<BarrelMinecartEntity>of(BarrelMinecartEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<MetalBarrelMinecartEntity>> METAL_BARREL_CART = register(
			"cart_metalbarrel",
			() -> Builder.<MetalBarrelMinecartEntity>of(MetalBarrelMinecartEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.7F)
	);
	public static final RegistryObject<EntityType<SawbladeEntity>> SAWBLADE = register(
			"sawblade",
			() -> Builder.<SawbladeEntity>of(SawbladeEntity::new, MobCategory.MISC)
					.sized(.75F, .2F)
	);

	private static <T extends Entity>
	RegistryObject<EntityType<T>> register(String name, Supplier<Builder<T>> prepare)
	{
		return REGISTER.register(name, () -> prepare.get().build(ImmersiveEngineering.MODID+":"+name));
	}
}
