/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class BulletItem<T> extends IEBaseItem implements IColouredItem
{
	private final IBullet<T> type;
	private final DataComponentType<T> component;

	public BulletItem(IBullet<T> type)
	{
		super(new Properties().component(IEDataComponents.getBulletData(type), type.getCodec().defaultValue()));
		this.type = type;
		this.component = IEDataComponents.getBulletData(type);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		type.addTooltip(stack.get(component), ctx, list, flag);
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return type.getColour(stack.get(component), pass).toInt();
	}

	public IBullet<?> getType()
	{
		return type;
	}

	public Entity createBullet(
			Level world,
			@Nullable Player shooter,
			Vec3 startPosition, Vec3 vecDir,
			ItemStack bulletStack, boolean electro
	)
	{
		T data = bulletStack.get(IEDataComponents.getBulletData(type));
		RevolvershotEntity bullet = new RevolvershotEntity(
				world,
				startPosition.x+vecDir.x, startPosition.y+vecDir.y, startPosition.z+vecDir.z,
				vecDir.x, vecDir.y, vecDir.z,
				type, data
		);
		bullet.bulletElectro = electro;
		bullet.setOwner(shooter);
		return type.getProjectile(shooter, data, bullet, false);
	}
}