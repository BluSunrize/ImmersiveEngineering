/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.asm;

import blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.ICollisionHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public class WireCollisionCallback
{
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.world.isRemote&&IEConfig.enableWireDamage&&e instanceof EntityLivingBase&&
				!e.isEntityInvulnerable(IEDamageSources.wireShock)&&
				!(e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.disableDamage))
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(e.world);
			WireCollisionData wireData = global.getCollisionData();
			Collection<WireCollisionData.CollisionInfo> atBlock = wireData.getCollisionInfo(p);
			if(atBlock!=null)
				for(WireCollisionData.CollisionInfo info : atBlock)
				{
					LocalWireNetwork local = info.getLocalNet();
					for(LocalNetworkHandler h : local.getAllHandlers())
						if(h instanceof ICollisionHandler)
							((ICollisionHandler)h).onCollided((EntityLivingBase)e, p, info);
				}
		}
	}
}
