import re
import sys

input_file = sys.argv[1]
file_ending_idx = re.search('\.obj.*$', input_file).start()
output_file = input_file[:file_ending_idx] + '_minimized' + input_file[file_ending_idx:]

PATTERN_START = '^(o|v|vn|vt|f|usemtl)'
PATTERN_FACE = '((?:\d+\/)+\d+)'


def split_corner(corner):
    """Split one corner of a face into vertex, texture and optionally normal"""
    split = corner.split('/')
    if len(split) == 3:
        return split
    else:
        return split[0], split[1], None


def fix_duplicates(array_in):
    """ Remove duplicates from an array and create a map to reassign values referring to it"""
    array_out = []
    reassign = {}
    for index, value in enumerate(array_in):
        # obj files start their indices for everything at 1
        if value in array_out:
            reassign[index + 1] = array_out.index(value) + 1
        else:
            array_out.append(value)
            reassign[index + 1] = len(array_out)
    return array_out, reassign


# Read file
with open(input_file, 'r') as f:
    lines = f.readlines()

# Parse all objects
file_header = ''
objects = []
cur_object = None
cur_mat = None
for line in lines:
    if not re.search(PATTERN_START, line):
        if not cur_object:
            file_header += line
        continue

    line = line.rstrip()
    if line.startswith('o '):
        # Append current object to list, start new object
        if cur_object:
            objects.append(cur_object)
        cur_object = {
            'name': line[2:],
            'vertices': [],
            'texture_coordinates': [],
            'vertex_normals': [],
            'faces': []
        }
    elif line.startswith('usemtl '):
        cur_mat = line[7:]
    elif line.startswith('v '):
        cur_object['vertices'].append(line)
    elif line.startswith('vt '):
        cur_object['texture_coordinates'].append(line)
    elif line.startswith('vn '):
        cur_object['vertex_normals'].append(line)
    elif line.startswith('f '):
        corners = re.findall(PATTERN_FACE, line)
        face = {'mat': cur_mat, 'verts': [], 'uvs': [], 'normals': [], 'size': len(corners)}
        for corner in corners:
            vert, tex, norm = split_corner(corner)
            face['verts'].append(int(vert))
            face['uvs'].append(int(tex))
            if norm:
                face['normals'].append(int(norm))
        cur_object['faces'].append(face)

# Wrap up
objects.append(cur_object)

for obj in objects:
    print('Handling object {name}'.format(name=obj['name']))
    # Get reduced lists and reassigning maps
    fixed_verts, reassign_verts = fix_duplicates(obj['vertices'])
    fixed_uvs, reassign_uvs = fix_duplicates(obj['texture_coordinates'])
    fixed_normals, reassign_normals = fix_duplicates(obj['vertex_normals'])
    # Log changes
    print('Removed {v} duplicate vertices, {vt} duplicate texture coords and {vn} duplicate normals'.format(
        v=len(obj['vertices']) - len(fixed_verts),
        vt=len(obj['texture_coordinates']) - len(fixed_uvs),
        vn=len(obj['vertex_normals']) - len(fixed_normals)
    ))

    # Set new lists
    obj['vertices'] = fixed_verts
    obj['texture_coordinates'] = fixed_uvs
    obj['vertex_normals'] = fixed_normals

    # Reassign indices in faces
    for face in obj['faces']:
        face['verts'] = [
            reassign_verts[x] if x in reassign_verts else x for x in face['verts']
        ]
        face['uvs'] = [
            reassign_uvs[x] if x in reassign_uvs else x for x in face['uvs']
        ]
        face['normals'] = [
            reassign_normals[x] if x in reassign_normals else x for x in face['normals']
        ]

# Write to output file
with open(output_file, 'w') as out_file:
    # Write the header
    out_file.write(file_header)

    for obj in objects:
        out_file.write('\n')
        # Start object
        out_file.write('o {}\n'.format(obj['name']))
        # Write vertices, textures and normals
        for vert in obj['vertices']:
            out_file.write(vert + '\n')
        for uv in obj['texture_coordinates']:
            out_file.write(uv + '\n')
        for vn in obj['vertex_normals']:
            out_file.write(vn + '\n')

        has_normals = len(obj['vertex_normals']) > 0

        last_mat = None
        for face in obj['faces']:
            # insert usemtl if material changed
            if face['mat'] != last_mat:
                last_mat = face['mat']
                out_file.write('usemtl {}\n'.format(last_mat))

            # build face string
            str_face = 'f'
            for idx in range(face['size']):
                if has_normals:
                    str_face += ' {v}/{vt}/{vn}'.format(
                        v=face['verts'][idx], vt=face['uvs'][idx], vn=face['normals'][idx]
                    )
                else:
                    str_face += ' {v}/{vt}'.format(
                        v=face['verts'][idx], vt=face['uvs'][idx]
                    )
            out_file.write(str_face + '\n')
