plugins {
	id("de.siphalor.tweed5.base-module")
}

dependencies {
	api(project(":tweed5-construct"))
	api(project(":tweed5-default-extensions"))
	api(project(":tweed5-weaver-pojo"))
}
