package com.lycanitesmobs.core.item;

public class GenericItem extends BaseItem {
	public String modelName;
	public final Properties properties;

	public GenericItem(Properties properties, String itemName) {
		super(properties);
        this.properties = properties;
        this.itemName = itemName;
		super.setup();
	}
}
