package blusunrize.immersiveengineering.common.data;

import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunCompleteHelper implements IDataProvider
{
	@Override
	public void act(DirectoryCache cache) throws IOException
	{
		Files.createFile(Paths.get("ie_data_gen_done"));
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Create a file to allow crashes in datagen to be detected";
	}
}
