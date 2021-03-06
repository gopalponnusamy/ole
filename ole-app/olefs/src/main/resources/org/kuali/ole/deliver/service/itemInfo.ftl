<#macro item oleNoticeBo oleNoticeContentConfigurationBo>

<table>
  <TR>
      <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Title")} :</TD>
      <#if oleNoticeBo.title ??>
          <TD>${oleNoticeBo.title}</TD>
      <#else>
          <TD</TD>
      </#if>

  </TR>
  <TR>
      <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Author")} :</TD>
      <#if oleNoticeBo.author??>
          <TD>${oleNoticeBo.author}</TD>
      <#else>
          <TD</TD>
      </#if>

  </TR>
    <TR>
        <TD>${oleNoticeContentConfigurationBo.getFieldLabel("CopyNumber")}  :</TD>
        <#if oleNoticeBo.copyNumber??>
            <TD>${oleNoticeBo.copyNumber}</TD>
        <#else>
            <TD</TD>
        </#if>

    </TR>

    <TR>
        <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Enumeration")}  :</TD>
        <#if oleNoticeBo.enumeration ??>
            <TD>${oleNoticeBo.enumeration}</TD>
        <#else>
            <TD</TD>
        </#if>

    </TR>
    <TR>
        <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Chronology")}  :</TD>
        <#if oleNoticeBo.chronology ??>
            <TD>${oleNoticeBo.chronology}</TD>
        <#else>
            <TD</TD>
        </#if>

    </TR>
    <TR>
        <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Call Number")} :</TD>
        <#if oleNoticeBo.itemCallNumber??>
            <TD>${oleNoticeBo.itemCallNumber}</TD>
        <#else>
            <TD</TD>
        </#if>

    </TR>
    <TR>
        <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Item Barcode")} :</TD>
        <#if oleNoticeBo.itemId ??>
            <TD>${oleNoticeBo.itemId}</TD>
        <#else>
            <TD</TD>
        </#if>

    </TR>
    <#if oleNoticeBo.noticeTitle == "Overdue Notice">
  <TR>
      <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Item was due")} :</TD>
      <#if oleNoticeBo.dueDateString??>
          <TD>${oleNoticeBo.dueDateString}</TD>
      <#else>
          <TD</TD>
      </#if>

  </TR>
    </#if>
    <#if oleNoticeBo.noticeTitle == "Lost">
        <TR>
            <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Library location")} :</TD>
            <#if oleNoticeBo.itemLibrary ??>
                <TD>${oleNoticeBo.itemLibrary}</TD>
            <#else>
                <TD</TD>
            </#if>

        </TR>
    </#if>

  <TR>
      <TD>${oleNoticeContentConfigurationBo.getFieldLabel("Library Shelving location")} :</TD>
      <#if oleNoticeBo.itemShelvingLocation ??>
          <TD>${oleNoticeBo.itemShelvingLocation}</TD>
      <#else>
          <TD</TD>
      </#if>
  </TR>

</table>

</#macro>