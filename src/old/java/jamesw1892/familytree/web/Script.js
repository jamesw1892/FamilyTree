function onlyShowLivingFunc(chkPassport) {
    var input, filter, table, tr, td, i, txtValue;
    table = document.getElementById("table");
    tr = table.getElementsByTagName("tr");

    // Loop through all table rows, and hide those who are deceased
    for (i = 0; i < tr.length; i++) {
        td = tr[i].getElementsByTagName("td")[4];
        if (td) {
            txtValue = td.textContent || td.innerText;
            if (txtValue.indexOf("Living") <= -1 && chkPassport.checked) {
                tr[i].style.display = "none";
            } else {
                tr[i].style.display = "";
            }
        }
    }
}