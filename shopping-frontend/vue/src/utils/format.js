export const formatImage = (url, fallback) => {
    return url || fallback || 'https://picsum.photos/200/200?random';
};