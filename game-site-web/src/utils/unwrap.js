export function unwrap(res) {
    if (res == null) return res

    // axios response
    if (res && typeof res === 'object' && 'data' in res) {
        const d = res.data
        if (d && typeof d === 'object' && 'data' in d) return d.data
        return d
    }

    // plain wrapped
    if (res && typeof res === 'object' && 'data' in res) return res.data

    return res
}

export default unwrap
