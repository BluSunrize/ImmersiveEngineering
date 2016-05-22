package blusunrize.immersiveengineering.common.blocks.cloth;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockClothDevice extends BlockIETileProvider
{
	public BlockClothDevice()
	{
		super("clothDevice", Material.cloth, PropertyEnum.create("type", BlockTypes_ClothDevice.class), ItemBlockClothDevice.class, IEProperties.FACING_ALL);
		setHardness(0.8F);
	}
	
	@Override
	public boolean isFullBlock()
	{
		return false;
	}
	@Override
	public boolean isFullCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

    @SideOnly(Side.CLIENT)
    public int getRenderColor(IBlockState state)
    {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        return 16777215;
    }
	
	@Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
//        if(entityIn.isSneaking())
//        {
//            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
//        }
//        else
//        {
            entityIn.fall(fallDistance, 0.0F);
//        }
    }
	@Override
	protected BlockState createBlockState() {
		BlockState base = super.createBlockState();
		IUnlistedProperty[] unlisted = (IUnlistedProperty[]) ((base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray():new IUnlistedProperty[0]);
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState) state;
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}

//	@Override
//	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
//	{
//		TileEntity tile = world.getTileEntity(x, y, z);
//		if(tile instanceof TileEntityBalloon)
//		{
//			ItemStack equipped = player.getCurrentEquippedItem();
//			if(Utils.isHammer(equipped))
//			{
//				((TileEntityBalloon)tile).style = ((TileEntityBalloon)tile).style==0?1:0;
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//			else if(equipped!=null && equipped.getItem() instanceof IShaderItem)
//			{
//				((TileEntityBalloon)tile).shader = equipped;
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//			else
//			{
//				int target = 0;
//				int style = ((TileEntityBalloon)tile).style;
//				if(side<2 && style==0)
//					target = (hitX<.375||hitX>.625)&&(hitZ<.375||hitZ>.625)?1:0;
//				else if(side>=2&&side<4)
//				{
//					if(style==0)
//						target = (hitX<.375||hitX>.625)?1:0;
//					else
//						target =(hitY>.5625&&hitY<.75)?1:0;
//				}
//				else if(side>=4)
//				{
//					if(style==0)
//						target = (hitZ<.375||hitZ>.625)?1:0;
//					else
//						target =(hitY>.5625&&hitY<.75)?1:0;
//				}
//				int heldDye = Utils.getDye(equipped);
//				if(heldDye==-1)
//					return false;
//				if(target==0)
//				{
//					if(((TileEntityBalloon)tile).colour0==heldDye)
//						return false;
//					((TileEntityBalloon)tile).colour0 = (byte)heldDye;
//				}
//				else
//				{
//					if(((TileEntityBalloon)tile).colour1==heldDye)
//						return false;
//					((TileEntityBalloon)tile).colour1 = (byte)heldDye;
//				}
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//		}
//		return false;
//	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_ClothDevice.values()[meta])
		{
		case CUSHION:
			return null;
		case BALLOON:
			return new TileEntityBalloon();
		}
		return null;
	}
}