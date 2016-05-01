package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockClothDevice extends BlockIETileProvider
{
	public BlockClothDevice()
	{
		super("clothDevice", Material.cloth, PropertyEnum.create("type", BlockTypes_ClothDevice.class), ItemBlockIEBase.class, IEProperties.FACING_ALL);
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