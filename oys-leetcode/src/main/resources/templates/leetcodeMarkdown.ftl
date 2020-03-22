# <#if paidOnly>$</#if>${feQuestionId}. [${difficulty}] ${titleCn}

**题目链接：**[https://leetcode-cn.com/problems/${titleSlug}](https://leetcode-cn.com/problems/${titleSlug})

---

<#if hasBug>
<Cards card="leetcode_${feQuestionId}_${titleSlug}"></Cards>
<#else>${htmlContent}
</#if >

---

```${type}
${txtContent}
```