console.log("this is scripting");

const toggleSidebar = () => {
    if ($(".sidebar").is(":visible")) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0");
    } else {
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

const search = () => {
    let query = $("#search-input").val();
    console.log(query);

    if (query == "") {
        $(".search-result").hide();
    } else {

        let url = `http://localhost:8181/search/${encodeURIComponent(query)}`;

        fetch(url)
            .then((response) => response.json())
            .then((data) => {
                console.log(data);

                let text = `<div class='list-group'>`;

                data.forEach((contact) => {
                    text += `
                        <a href="/user/contact/${contact.cid}"
                           class='list-group-item list-group-item-action'>
                           ${contact.name}
                        </a>`;
                });

                text += `</div>`;

                $(".search-result").html(text);
                $(".search-result").show();
            })
            .catch((error) => {
                console.error("Error:", error);
            });

        console.log("searching...");
    }
};