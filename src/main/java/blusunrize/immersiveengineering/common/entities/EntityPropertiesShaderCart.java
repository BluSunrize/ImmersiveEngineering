package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class EntityPropertiesShaderCart implements IExtendedEntityProperties
{
	public static final String PROPERTY_NAME = "IEShaderCart";
	private ItemStack shader = null;

	public ItemStack getShader()
	{
		return shader;
	}
	public void setShader(ItemStack stack)
	{
		this.shader = stack;
	}
	
	@Override
	public void init(Entity entity, World world)
	{
	}
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		if(shader!=null)
			compound.setTag("shader", shader.writeToNBT(new NBTTagCompound()));
	}
	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		shader = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("shader"));
	}
	@Override
	public String toString()
	{
		return "Shader: "+getShader();
	}
}