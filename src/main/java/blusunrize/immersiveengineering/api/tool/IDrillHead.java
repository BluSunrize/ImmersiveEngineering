/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author BluSunrize - 28.05.2015
 * <p>
 * An interface for items to make custom drill heads
 */
public interface IDrillHead
{
	/**
	 * Called before a block is broken by the head
	 * Return true to prevent the block from being broken
	 */
	boolean beforeBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player);

	/**
	 * Called after a block is broken by the head
	 * Damage should not be applied here but in the specific method.
	 */
	void afterBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player);

	/**
	 * @return A list of BlockPos that will be dug in addition to the targeted block
	 */
	ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, RayTraceResult mop);

	/**
	 * @return The mining level of the drill
	 */
	int getMiningLevel(ItemStack head);

	/**
	 * @return The speed of the drill
	 */
	float getMiningSpeed(ItemStack head);

	/**
	 * @return The damage the head does when equipped on the drill
	 */
	float getAttackDamage(ItemStack head);

	/**
	 * @return The current damage of the drill head
	 * Used to determine whether the head can be used
	 */
	int getHeadDamage(ItemStack head);

	/**
	 * @return The maximum damage of the dril head
	 * Used to determine whether the head can be used
	 */
	int getMaximumHeadDamage(ItemStack head);

	/**
	 * Apply damage to the drill head here
	 */
	void damageHead(ItemStack head, int damage);

	/**
	 * Return the texture of the drill head
	 * Look at IE's default texture for the UV layout
	 * This IIcon should be stitched in the item sheet
	 */
	@SideOnly(Side.CLIENT)
	TextureAtlasSprite getDrillTexture(ItemStack drill, ItemStack head);
}
