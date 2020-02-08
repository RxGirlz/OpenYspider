<#list fileNames as name>
git add docs${sidebarSlice}${name}
git commit -m "add ${name}"
</#list>