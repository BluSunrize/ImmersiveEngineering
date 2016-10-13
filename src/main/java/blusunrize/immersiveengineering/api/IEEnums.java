package blusunrize.immersiveengineering.api;

import net.minecraft.util.IStringSerializable;

public class IEEnums
{
	public enum SideConfig implements IStringSerializable
	{
		NONE("none"),
		INPUT("in"),
		OUTPUT("out");

		final String texture;

		SideConfig(String texture)
		{
			this.texture = texture;
		}

		@Override
		public String getName()
		{
			return this.toString().toLowerCase();
		}

		public String getTextureName()
		{
			return texture;
		}
	}
}