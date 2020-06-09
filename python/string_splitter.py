import textwrap, math, page_special_loader

# https://stackoverflow.com/questions/42070323/split-on-spaces-not-inside-parentheses-in-python
def tokenize(sentence,separator=' ',lparen='<(',rparen=')>'):
    nb_brackets=0
    sentence = sentence.strip(separator) # get rid of leading/trailing seps

    l=[0]
    for i,c in enumerate(sentence):
        if c in lparen:
            if nb_brackets==0: # when opening the first bracket, split text
                l.append(i)
            nb_brackets+=1
        elif c in rparen:
            nb_brackets-=1
            if nb_brackets==0: # when closing last first bracket, split text
                l.append(i+1)
        elif c==separator and nb_brackets==0: # when hitting a separator and not in brackets, split text
            l.append(i)
        # handle malformed string
        if nb_brackets<0:
            raise Exception('Syntax error')

    l.append(len(sentence))
    # handle missing closing parentheses
    if nb_brackets>0:
        raise Exception('Syntax error')

    return([sentence[i:j].strip(separator) for i,j in zip(l,l[1:])])


special_heights = {
    'crafting': page_special_loader.get_height_crafting,
    'image': page_special_loader.get_height_image,
    'item_display': page_special_loader.get_height_items,
    'table': page_special_loader.get_height_table,
    'blueprint': page_special_loader.get_height_blueprint,
    'multiblock': page_special_loader.get_height_multiblock,
}

max_width = 31
max_height = 16

def split_pages(text, specials):
    word_list = tokenize(text)
    pages = []
    page_text = ''
    current_line = ''
    line_count = 0
    anchors = ['start']
    for word in word_list:
        end_page = False
        anchor = None
        if word == '<np>': #page break
            end_page = True
        elif word.startswith('<&'): #anchor
            #print 'got anchor {}'.format(word)
            if len(page_text):
                end_page = True
            anchor = word[2:-1]
        elif len(word):
            current_line += ' '+word
            if len(current_line.strip()) > max_width:
                line_count += 1
                current_line = word
            if line_count > max_height:
                end_page = True
                word_list.insert(0, word)
            if not end_page:
                page_text += ' '+word

        if end_page:
            page_text = page_text.strip()
            if len(anchors) or len(page_text):
                pages.append({
                    'anchors': anchors,
                    'text': page_text,
                })
            page_text = ''
            anchors = []
            current_line = ''
            line_count = 0
        # if anchor was set, append it now to anchor list (which may be new page)
        # if there is a special element associated, increment line count
        if anchor:
            anchors.append(anchor)
            if anchor in specials:
                special = specials[anchor]
                if type(special) is list:
                    for el_spec in special:
                        if el_spec and 'type' in el_spec:
                            line_count += special_heights[el_spec['type']](el_spec)
                else:
                    line_count += special_heights[special['type']](special)
    # clean up remaining
    if len(anchors) or len(page_text):
        pages.append({
            'anchors': anchors,
            'text': page_text,
        })

    #print pages
    #print '\n\n'
    return pages