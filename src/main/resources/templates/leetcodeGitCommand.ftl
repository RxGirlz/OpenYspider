<#list fileNames as name>
git add docs${sidebarSlice}${name}.md
git add docs/.vuepress/public/htmlSrc/${name}.html
git commit -m "add ${name}"
</#list>