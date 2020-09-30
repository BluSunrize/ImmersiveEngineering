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


def fix_duplicates(array_in, segments):
    """ Remove duplicates from an array and create a map to reassign values referring to it"""
    array_out = []
    reassign = {}
    out_segments = []
    prev_start = 0
    for end in segments:
        sub_array_in = array_in[prev_start:end]
        offset = len(array_out)
        sub_array_out = []
        for index, value in enumerate(sub_array_in):
            # obj files start their indices for everything at 1
            if value in sub_array_out:
                reassign[prev_start + index + 1] = offset + sub_array_out.index(value) + 1
            else:
                sub_array_out.append(value)
                reassign[prev_start + index + 1] = offset + len(sub_array_out)
        array_out += sub_array_out
        out_segments.append(len(array_out))
        prev_start = end
    return array_out, reassign, out_segments


# Read file
with open(input_file, 'r') as f:
    lines = f.readlines()

# Parse all objects
file_header = ''

# Store all objects
vertices = []
texture_coordinates = []
vertex_normals = []
objects = []
cur_object = None
cur_mat = None


def finish_object():
    """ Finish the current object, setting its max indices and appending it to the list"""
    if cur_object:
        cur_object['max_v'] = len(vertices)
        cur_object['max_vt'] = len(texture_coordinates)
        cur_object['max_vn'] = len(vertex_normals)
        objects.append(cur_object)


for line in lines:
    if not re.search(PATTERN_START, line):
        if not cur_object:
            file_header += line
        continue

    line = line.rstrip()
    if line.startswith('o '):
        # Append current object to list, start new object
        finish_object()
        cur_object = {
            'name': line[2:],
            'faces': []
        }
    elif line.startswith('usemtl '):
        cur_mat = line[7:]
    elif line.startswith('v '):
        vertices.append(line)
    elif line.startswith('vt '):
        texture_coordinates.append(line)
    elif line.startswith('vn '):
        vertex_normals.append(line)
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
finish_object()

# Get reduced lists and reassigning maps
fixed_verts, reassign_verts, vert_segments = fix_duplicates(vertices, [obj['max_v'] for obj in objects])
fixed_uvs, reassign_uvs, uv_segments = fix_duplicates(texture_coordinates, [obj['max_vt'] for obj in objects])
fixed_normals, reassign_normals, norm_segments = fix_duplicates(vertex_normals, [obj['max_vn'] for obj in objects])

# Log changes
print('Removed {v} duplicate vertices, {vt} duplicate texture coords and {vn} duplicate normals'.format(
    v=len(vertices) - len(fixed_verts),
    vt=len(texture_coordinates) - len(fixed_uvs),
    vn=len(vertex_normals) - len(fixed_normals)
))

offsetV = offsetVt = offsetVn = 0
for index, obj in enumerate(objects):
    print('Handling object {name}'.format(name=obj['name']))

    # Assign partial lists on the object
    obj['vertices'] = fixed_verts[offsetV:vert_segments[index]]
    obj['texture_coordinates'] = fixed_uvs[offsetVt:uv_segments[index]]
    obj['vertex_normals'] = fixed_normals[offsetVn:norm_segments[index]]
    offsetV = vert_segments[index]
    offsetVt = uv_segments[index]
    offsetVn = norm_segments[index]
    print(' object contains {} verts, {} uvs and {} normals'.format(
        len(obj['vertices']),len(obj['texture_coordinates']),len(obj['vertex_normals'])
    ))

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
