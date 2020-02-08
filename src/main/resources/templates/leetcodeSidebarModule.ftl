let path = "${sidebarSlice}";
module.exports = [
<#list fileNames as name>
  path + "${name}",
</#list>
];
