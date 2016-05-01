package blusunrize.immersiveengineering.api.tool;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author BluSunrize - 28.05.2015
 *
 * An interface for items to make custom drill heads
 */
public interface IDrillHead
{
	/**Called before a block is broken by the head
	 * Return true to prevent the block from being broken
	 */
	public boolean beforeBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player);
	
	/**Called after a block is broken by the head
	 * Damage should not be applied here but in the specific method.
	 */
	public void afterBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player);
	
	/**@return A list of BlockPos that will be dug in addition to the targeted block
	 */
	public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, EntityPlayer player, MovingObjectPosition mop);
	
	/**@return The mining level of the drill
	 */
	public int getMiningLevel(ItemStack head);
	
	/**@return The speed of the drill
	 */
	public float getMiningSpeed(ItemStack head);
	
	/**@return The damage the head does when equipped on the drill
	 */
	public float getAttackDamage(ItemStack head);

	/**@return The current damage of the drill head
	 * Used to determine whether the head can be used
	 */
	public int getHeadDamage(ItemStack head);
	/**@return The maximum damage of the dril head
	 * Used to determine whether the head can be used
	 */
	public int getMaximumHeadDamage(ItemStack head);
	
	/**Apply damage to the drill head here
	 */
	public void damageHead(ItemStack head, int damage);
	
	/**Return the texture of the drill head
	 * Look at IE's default texture for the UV layout
	 * This IIcon should be stitched in the item sheet
	 */
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getDrillTexture(ItemStack drill, ItemStack head);
}
