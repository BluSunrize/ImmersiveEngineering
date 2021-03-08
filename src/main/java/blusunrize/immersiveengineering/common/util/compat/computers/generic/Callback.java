package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Callback<T>
{
	private final List<Callback<? super T>> additional = new ArrayList<>();

	public String renameMethod(String javaName)
	{
		return javaName;
	}

	protected final void addAdditional(Callback<? super T> toAdd)
	{
		additional.add(toAdd);
	}

	public final List<Callback<? super T>> getAdditionalCallbacks()
	{
		return Collections.unmodifiableList(this.additional);
	}
}
