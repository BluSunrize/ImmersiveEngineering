let entries = {};

window.onload = function () {
    let canvas = document.getElementById("page");
    let ctx = canvas.getContext("2d");
    ctx.imageSmoothingEnabled = false;

    let img = new Image;
    img.onload = function () {
        canvas.width = 166;
        canvas.height = 200;
        ctx.drawImage(img, 20, 0, 166, 200, 0, 0, 166, 200);
    };
    img.src = '../assets/manual.png';
};

$.ajax({
    url: './entry_list.json',
    success: function (result) {
        entries = result;
        switch_to_index();
    }
});

let selected_entry = null;
let current_pages = null;

function load_entry(entry) {
    selected_entry = entry;
    console.log('open ' + selected_entry);

    if (selected_entry) {
        switch_to_entry();
    } else {
        switch_to_index();
    }
}

function switch_to_index() {
    let content_field = $('.manual div.content');
    content_field.empty();
    let list = $('<ul></ul>');
    for (let key in entries) {
        let entry = entries[key];
        let li = $(`<li name="${key}">${entry['title']}</li>`);
        li.click(() => load_entry(entry));
        list.append(li);
    }
    content_field.append(list);
}

function switch_to_entry() {
    let content_field = $('.manual div.content');
    content_field.empty();
    let button_back = $(`<button class="back"></button>`);
    button_back.click(() => load_entry(null));
    content_field.append(button_back);
    $.ajax({
        url: selected_entry['url'],
        success: function (result) {
            // append title & subtitle
            content_field.append(`<h2>${result['title']}</h2>`);
            content_field.append(`<h3>${result['subtitle']}</h3>`);

            // set page number tracking
            current_pages = {
                selected: 0,
                max: result['pages'].length - 1
            }
            console.log(current_pages)
            // append pages
            let div_pages = $('<div class="pages"></div>');
            result['pages'].forEach((page, page_nr) => {
                let div_singlepage = $(`<div class="page_${page_nr}"></div>`);

                // append special elements
                for (let anchor of page['anchors'])
                    if (result['specials'][anchor]) {
                        div_singlepage.append(build_special_element(result['specials'][anchor]));
                    }

                // append paragraphs
                let lines = page['text'].split('\n');
                for (let line of lines) {
                    div_singlepage.append(parse_line(line));
                }
                if (page_nr > 0)
                    div_singlepage.hide();
                div_pages.append(div_singlepage);
            });
            content_field.append(div_pages);
            content_field.append(build_footer());
        }
    });
}

function build_footer() {
    let footer = $('<footer></footer>');
    let button_prev = $('<button class="page_prev off"></button>');
    button_prev.click(() => switch_page(false));
    let button_next = $('<button class="page_next"></button>');
    button_next.click(() => switch_page(true));
    button_next.toggleClass('off', current_pages['max'] === 0);
    footer.append(button_prev);
    footer.append('<span class="line_number">1</span>');
    footer.append(button_next);
    console.log(footer)
    return footer;
}

function switch_page(next) {
    console.log('switch page!')
    if (next)
        current_pages['selected']++;
    else
        current_pages['selected']--;
    $('span.line_number').text(current_pages['selected'] + 1)
    $('button.page_prev').toggleClass('off', current_pages['selected'] === 0)
    $('button.page_next').toggleClass('off', current_pages['selected'] === current_pages['max'])
    $('div[class^="page_"]').hide();
    $('div.page_' + current_pages['selected']).show();
}

const regex_link_basic = /<link;(.*?);(.*?)>/g

function parse_line(line) {
    let paragraph = $('<p></p>');

    let matches = line.matchAll(regex_link_basic);
    let last_index = 0;
    for (let arr of matches) {
        // Put everything before the link into the paragraph
        let pre = line.slice(last_index, arr.index);
        paragraph.append(`<span>${pre}</span>`);

        // Append the link
        let linked_entry = entries[arr[1]];
        if (linked_entry == null)
            console.log("missing page " + arr[1]);
        else {
            let link = $(`<span class="link" title="${linked_entry.title}">${arr[2]}</span>`);
            link.click(() => load_entry(linked_entry));
            paragraph.append(link);
        }
        // Cut line to remaining content
        last_index = arr.index + arr[0].length
    }
    // Add the rest
    paragraph.append(`<span>${line.substr(last_index)}</span>`);
    return paragraph;
}

function build_special_element(special_element) {
    console.log('add special element ');
    console.log(special_element);
    if (special_element['type'] === 'crafting') {
        let container = $(`<div class="crafting_container" grid_height="${special_element['height']}"></div>`);
        for(let recipe of special_element['recipes']){
            let recipe_div = $(`<div class="recipe" grid_height="${special_element['height']}" grid_width="${special_element['width']}"></div>`);
            recipe_div.append('crafting recipe for '+JSON.stringify(recipe['output']));
            container.append(recipe_div);
        }
        return container;
    }
    return null;
}