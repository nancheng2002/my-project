import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

marked.setOptions({
    breaks: true,
    gfm: true,
    highlight(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            return hljs.highlight(code, { language: lang }).value
        }
        return hljs.highlightAuto(code).value
    },
})

export function renderMarkdown(md = '') {
    const raw = marked.parse(md || '')
    return DOMPurify.sanitize(raw)
}

export default renderMarkdown
