package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.lib.manual.ManualPages;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.ComputerCraftAPI;

public class ComputercraftHelper extends IECompatModule
{

	@Override
	public void preInit() {}

	@Override
	public void init()
	{
		ComputerCraftAPI.registerPeripheralProvider(new IEPeripheralProvider());
	}

	@Override
	public void postInit()
	{
		if (FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		{
			ManualHelper.getManual().addEntry("computer.general", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.general0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general2"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general3"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general4"));
			ManualHelper.getManual().addEntry("arcfurnace", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace2"));
			ManualHelper.getManual().addEntry("computer.sampleDrill", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.sampleDrill0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.sampleDrill1"));
			ManualHelper.getManual().addEntry("crusher", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.crusher0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.crusher1"));
			ManualHelper.getManual().addEntry("dieselgen", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.dieselgen0"));
			ManualHelper.getManual().addEntry("eMeter", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.energymeter0"));
			ManualHelper.getManual().addEntry("excavator", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.excavator0"));
			ManualHelper.getManual().addEntry("computer.squeezerAndFermenter", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter2"));
		}
	}

}
