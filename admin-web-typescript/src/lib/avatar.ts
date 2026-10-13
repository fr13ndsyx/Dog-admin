import defaultAvatar from '/@/assets/images/default-avatar.svg';

export { defaultAvatar };

export function showDefaultAvatar(event: Event): void {
  const image = event.currentTarget as HTMLImageElement;
  if (image.getAttribute('src') !== defaultAvatar) {
    image.src = defaultAvatar;
  }
}
