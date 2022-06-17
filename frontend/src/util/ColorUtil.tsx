export function getBackgroundColor(background: string): string {
    if (background === 'OFF_WHITE_D') {
        return 'rgb(161, 158, 153)';
    } else if (background === 'OFF_WHITE_C') {
        return 'rgb(187, 189, 183)';
    } else if (background === 'OFF_WHITE_B') {
        return 'rgb(223, 223, 221)';
    } else if (background === 'OFF_WHITE_A') {
        return 'rgb(239, 243, 242)';
    } else if (background === 'RED') {
        return 'rgb(198, 47, 65)';
    } else if (background === 'DARK_BLUE') {
        return 'rgb(46, 73, 116)';
    } else if (background === 'COOL_GRAY') {
        return 'rgb(64, 62, 82)';
    } else if (background === 'DARK_PURPLE') {
        return 'rgb(70, 67, 111)';
    } else {
        return 'rgb(187, 189, 183)';
    }
}

export function textColor(colorString: string): string {
    if (colorString.includes("OFF_WHITE")) {
        return "text-black";
    } else {
        return "text-white"
    }
}
