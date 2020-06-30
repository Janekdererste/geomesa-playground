export default class Rectangle {
    private left: number
    private right: number
    private top: number
    private bottom: number

    constructor(left: number, right: number, top: number, bottom: number) {
        this.left = left
        this.right = right
        this.top = top
        this.bottom = bottom
    }

    get Top() {
        return this.top
    }

    get Bottom() {
        return this.bottom
    }

    get Left() {
        return this.left
    }

    get Right() {
        return this.right
    }

    get CenterX() {
        return (this.left + this.right) / 2
    }

    get CenterY() {
        return (this.top + this.bottom) / 2
    }

    get Height() {
        return Math.abs(this.bottom - this.top)
    }

    get Width() {
        return Math.abs(this.left - this.right)
    }

    public static createFromCenterWithDimensions(centerX: number, centerY: number, width: number, height: number) {
        const left = centerX - width / 2
        const right = centerX + width / 2
        const top = centerY + height / 2
        const bottom = centerY - height / 2
        return new Rectangle(left, right, top, bottom)
    }

    public static createFromOffset(rect: Rectangle, offsetX: number, offsetY: number) {
        const left = rect.left + offsetX
        const right = rect.right + offsetX
        const top = rect.top + offsetY
        const bottom = rect.bottom + offsetY
        return new Rectangle(left, right, top, bottom)
    }

    public static createVerticallyFlipped(rect: Rectangle) {
        return new Rectangle(rect.left, rect.right, -rect.top, -rect.bottom)
    }

    public static createFromExtend(extend: [number, number, number, number]) {
        return new Rectangle(extend[0], extend[2], extend[3], extend[1])
    }
}