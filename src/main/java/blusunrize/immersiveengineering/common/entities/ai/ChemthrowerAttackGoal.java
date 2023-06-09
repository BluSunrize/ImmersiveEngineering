/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.ai;

import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

public class ChemthrowerAttackGoal<T extends Bulwark> extends ShieldCombatGoal<T>
{
	private int counter = -1;
	private final FluidStack fluidStack = new FluidStack(IEFluids.ETHANOL.getStill(), 1000);

	public ChemthrowerAttackGoal(T mob, float attackRadius)
	{
		super(mob, attackRadius, new IntRange(120, 180), new IntRange(0, 1));
	}

	@Override
	protected boolean isHoldingWeapon()
	{
		return this.mob.isHolding(is -> is.getItem() instanceof ChemthrowerItem);
	}

	@Override
	public void tick()
	{
		super.tick();
		if(this.combatState==ShieldCombatState.ATTACK&&!this.mob.isAiming())
			this.mob.setAiming(true);
		if(this.combatState!=ShieldCombatState.ATTACK&&this.mob.isAiming())
			this.mob.setAiming(false);
	}

	@Override
	boolean performAttack()
	{
		counter++;
		Vec3 v = this.mob.getLookAngle();
		int split = 8;
		boolean isGas = fluidStack.getFluid().is(Tags.Fluids.GASEOUS);

		float scatter = isGas?.25f: .15f;
		float range = isGas?.5f: 1f;

		RandomSource random = this.mob.getRandom();
		for(int i = 0; i < split; i++)
		{
			Vec3 vecDir = v.add(random.nextGaussian()*scatter, random.nextGaussian()*scatter, random.nextGaussian()*scatter);
			ChemthrowerShotEntity chem = new ChemthrowerShotEntity(this.mob.level(), this.mob, vecDir.x*0.25, vecDir.y*0.25, vecDir.z*0.25, fluidStack);

			// Apply momentum from the player.
			chem.setDeltaMovement(this.mob.getDeltaMovement().add(vecDir.scale(range)));
			chem.setSecondsOnFire(10);
			if(!this.mob.level().isClientSide)
				this.mob.level().addFreshEntity(chem);
		}
		if(counter%4==0)
			this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), IESounds.sprayFire.get(), SoundSource.PLAYERS, .5f, 1.5f);

		if(counter >= 40)
		{
			counter = 0;
			return true;
		}
		return false;
	}

}
